package com.amalitech.training.readingroom.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amalitech.training.readingroom.member.MemberType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** The loan-period table from the original program: book 14/28, audio kit 7/14. */
class LoanPeriodTest {

    @ParameterizedTest(name = "{0} borrowed by a {1} member: {2} days")
    @CsvSource({
        "BOOK,      STANDARD, 14",
        "BOOK,      STUDENT,  28",
        "AUDIO_KIT, STANDARD,  7",
        "AUDIO_KIT, STUDENT,  14"
    })
    void loanLengthDependsOnItemKindAndMemberType(String itemKind, MemberType memberType, int expectedDays) {
        LendingItem item = itemKind.equals("BOOK") ? new Book("B-0001", "Any title") : new AudioKit("A-0001", "Any title");

        assertEquals(itemKind, item.typeCode());
        assertEquals(expectedDays, item.loanPeriod().daysFor(memberType));
    }

    @Test
    @DisplayName("Loan periods must be at least one day")
    void periodsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new LoanPeriod(0, 14));
        assertThrows(IllegalArgumentException.class, () -> new LoanPeriod(7, -1));
    }

    @Test
    @DisplayName("A member type is required to work out a loan period")
    void memberTypeIsRequired() {
        assertThrows(NullPointerException.class, () -> new LoanPeriod(14, 28).daysFor(null));
    }

    @Test
    @DisplayName("Items need a title")
    void itemsNeedATitle() {
        assertThrows(IllegalArgumentException.class, () -> new Book("B-0001", "  "));
        assertThrows(IllegalArgumentException.class, () -> new AudioKit("A-0001", null));
    }
}
