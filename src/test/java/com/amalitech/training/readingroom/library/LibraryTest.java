package com.amalitech.training.readingroom.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.amalitech.training.readingroom.item.AudioKit;
import com.amalitech.training.readingroom.item.Book;
import com.amalitech.training.readingroom.member.Member;
import com.amalitech.training.readingroom.member.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The lending rules, tested directly on Library without any console output. */
class LibraryTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 11);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneOffset.UTC);

    private Library library;
    private Book novel;
    private Member kofi;
    private Member ama;

    @BeforeEach
    void setUp() {
        library = new Library("Test Library", FIXED_CLOCK);
        novel = new Book("B-0001", "Things Fall Apart");
        kofi = new Member("M-0001", "Kofi Mensah", MemberType.STANDARD);
        ama = new Member("M-0002", "Ama Owusu", MemberType.STUDENT);
        library.addItem(novel);
        library.addMember(kofi);
        library.addMember(ama);
    }

    // ---- the rule the business says must never break -------------------------------

    @Test
    @DisplayName("An item already on loan cannot be lent again, and the first loan is untouched")
    void itemCannotBeOnTwoLoans() throws ItemAlreadyOnLoanException {
        Loan kofisLoan = library.borrow(kofi, novel);

        ItemAlreadyOnLoanException refused =
                assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, novel));

        assertEquals(1, refused.blockingLoanNumber());
        assertEquals("Kofi Mensah", refused.borrowerName());
        assertEquals(List.of(kofisLoan), library.activeLoans());
        assertEquals(kofi, library.activeLoanFor(novel).member());
        assertTrue(library.loansFor(ama).isEmpty());
    }

    @Test
    @DisplayName("The refusal message names the item but not the borrower, so it is safe to log")
    void refusalMessageDoesNotContainBorrowerName() throws ItemAlreadyOnLoanException {
        library.borrow(kofi, novel);

        ItemAlreadyOnLoanException refused =
                assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, novel));

        assertTrue(refused.getMessage().contains("Things Fall Apart"));
        assertFalse(refused.getMessage().contains("Kofi"));
    }

    @Test
    @DisplayName("A member's toString shows the member ID but not the name, so it is safe to log")
    void memberToStringDoesNotContainName() {
        assertTrue(kofi.toString().contains("M-0001"));
        assertFalse(kofi.toString().contains("Kofi"));
    }

    @Test
    @DisplayName("The same member borrowing the same item twice is also refused")
    void sameMemberCannotBorrowTwice() throws ItemAlreadyOnLoanException {
        library.borrow(kofi, novel);

        assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(kofi, novel));
        assertEquals(1, library.activeLoans().size());
    }

    @Test
    @DisplayName("A refused borrow does not use up a loan number")
    void refusedBorrowDoesNotUseALoanNumber() throws ItemAlreadyOnLoanException {
        AudioKit kit = new AudioKit("A-0001", "French Audio Course");
        library.addItem(kit);
        library.borrow(kofi, novel);
        assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, novel));

        Loan next = library.borrow(ama, kit);

        assertEquals(2, next.number());
    }

    @Test
    @DisplayName("A new loan records today's date and its due date")
    void loanRecordsBorrowAndDueDates() throws ItemAlreadyOnLoanException {
        Loan loan = library.borrow(kofi, novel);

        assertEquals(TODAY, loan.borrowedOn());
        assertEquals(14, loan.loanDays());
        assertEquals(TODAY.plusDays(14), loan.dueOn());
        assertTrue(loan.isActive());
    }

    // ---- returns -----------------------------------------------------------------

    @Test
    @DisplayName("Returning one copy does not close the loan on another copy with the same title")
    void returnClosesOnlyThatCopysLoan() throws ItemAlreadyOnLoanException {
        Book secondCopy = new Book("B-0002", "Things Fall Apart");
        library.addItem(secondCopy);
        library.borrow(kofi, novel);
        Loan amasLoan = library.borrow(ama, secondCopy);

        assertTrue(library.returnItem(novel));

        assertFalse(library.isOnLoan(novel));
        assertEquals(List.of(amasLoan), library.activeLoans());
        Loan closed = library.loansFor(kofi).get(0);
        assertFalse(closed.isActive());
        assertEquals(TODAY, closed.returnedOn());
    }

    @Test
    @DisplayName("A returned item can be lent again, and loan numbering continues")
    void returnedItemCanBeLentAgain() throws ItemAlreadyOnLoanException {
        library.borrow(kofi, novel);
        library.returnItem(novel);

        Loan second = library.borrow(ama, novel);

        assertEquals(2, second.number());
        assertEquals(ama, library.activeLoanFor(novel).member());
        assertEquals(1, library.loansFor(kofi).size());
    }

    @Test
    @DisplayName("Returning an item that is not on loan changes nothing, as in the original")
    void returnOfItemNotOnLoanChangesNothing() {
        assertFalse(library.returnItem(novel));
        assertTrue(library.activeLoans().isEmpty());
        assertFalse(library.isOnLoan(novel));
    }

    @Test
    @DisplayName("Asking for the active loan of an item that is not out is an error")
    void activeLoanOfAvailableItemThrows() {
        assertThrows(IllegalStateException.class, () -> library.activeLoanFor(novel));
    }

    // ---- encapsulation and validation ----------------------------------------------

    @Test
    @DisplayName("Lists returned by the library cannot be used to change its records")
    void returnedListsAreReadOnly() throws ItemAlreadyOnLoanException {
        library.borrow(kofi, novel);

        assertThrows(UnsupportedOperationException.class, () -> library.activeLoans().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.items().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.members().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.loansFor(kofi).clear());
    }

    @Test
    @DisplayName("Items and members must be registered with this library")
    void unregisteredItemsAndMembersAreRejected() {
        Book unregistered = new Book("B-0099", "Not in the catalogue");
        // Same name and type as Kofi, but a different member ID: a different person who never registered.
        Member lookalike = new Member("M-0099", "Kofi Mensah", MemberType.STANDARD);

        assertThrows(IllegalArgumentException.class, () -> library.borrow(kofi, unregistered));
        assertThrows(IllegalArgumentException.class, () -> library.borrow(lookalike, novel));
        assertThrows(IllegalArgumentException.class, () -> library.returnItem(unregistered));
    }

    @Test
    @DisplayName("The same item or member cannot be registered twice")
    void duplicateRegistrationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> library.addItem(novel));
        assertThrows(IllegalArgumentException.class, () -> library.addMember(kofi));
    }

    @Test
    @DisplayName("Null and blank values are rejected where they enter")
    void invalidInputIsRejected() {
        assertThrows(NullPointerException.class, () -> library.borrow(null, novel));
        assertThrows(NullPointerException.class, () -> library.borrow(kofi, null));
        assertThrows(NullPointerException.class, () -> library.addItem(null));
        assertThrows(IllegalArgumentException.class, () -> new Member("M-0003", "", MemberType.STUDENT));
        assertThrows(NullPointerException.class, () -> new Member("M-0002", "Ama Owusu", null));
        assertThrows(IllegalArgumentException.class, () -> new Library(" ", FIXED_CLOCK));
        assertThrows(IllegalArgumentException.class, () -> new Book(" ", "Things Fall Apart"));
        assertThrows(IllegalArgumentException.class, () -> new Member(null, "Kofi Mensah", MemberType.STANDARD));
    }

    // ---- identity: copy IDs and member IDs ---------------------------------------------

    @Test
    @DisplayName("Two copies with the same title are different items")
    void copiesAreDistinct() {
        assertNotEquals(new Book("B-0002", "Things Fall Apart"), novel);
    }

    @Test
    @DisplayName("Two objects with the same copy ID are the same copy, whatever their title")
    void sameCopyIdIsSameCopy() {
        Book sameBarcode = new Book("B-0001", "A Different Title");

        assertEquals(novel, sameBarcode);
        assertEquals(novel.hashCode(), sameBarcode.hashCode());
        assertThrows(IllegalArgumentException.class, () -> library.addItem(sameBarcode));
    }

    @Test
    @DisplayName("Two members with the same name but different IDs are two people")
    void sameNameDifferentIdIsADifferentMember() throws ItemAlreadyOnLoanException {
        Member otherKofi = new Member("M-0003", "Kofi Mensah", MemberType.STANDARD);
        library.addMember(otherKofi);

        library.borrow(kofi, novel);

        assertNotEquals(kofi, otherKofi);
        assertEquals(1, library.loansFor(kofi).size());
        assertTrue(library.loansFor(otherKofi).isEmpty());
    }

    @Test
    @DisplayName("A member ID can only be registered once")
    void duplicateMemberIdIsRejected() {
        Member sameCard = new Member("M-0001", "Someone Else", MemberType.STUDENT);

        assertThrows(IllegalArgumentException.class, () -> library.addMember(sameCard));
    }
}
