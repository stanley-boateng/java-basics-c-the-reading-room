package com.amalitech.training.readingroom.desk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.amalitech.training.readingroom.item.AudioKit;
import com.amalitech.training.readingroom.item.Book;
import com.amalitech.training.readingroom.library.Library;
import com.amalitech.training.readingroom.member.Member;
import com.amalitech.training.readingroom.member.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the volunteer sees. These tests pin the console output to the original program's
 * wording, so they are the evidence that behaviour is preserved.
 */
class LendingDeskTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneOffset.UTC);

    private ByteArrayOutputStream console;
    private Library library;
    private LendingDesk desk;

    @BeforeEach
    void setUp() {
        console = new ByteArrayOutputStream();
        library = new Library("The Reading Room", FIXED_CLOCK);
        desk = new LendingDesk(library, new PrintStream(console, true, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("The original afternoon prints the original output, except the double loan is refused")
    void originalAfternoon() {
        Book novel = new Book("B-0001", "Things Fall Apart");
        AudioKit audioCourse = new AudioKit("A-0001", "French Audio Course");
        Member kofi = new Member("M-0001", "Kofi Mensah", MemberType.STANDARD);
        Member ama = new Member("M-0002", "Ama Owusu", MemberType.STUDENT);
        library.addItem(novel);
        library.addItem(audioCourse);
        library.addMember(kofi);
        library.addMember(ama);

        assertTrue(desk.borrow(kofi, novel));
        assertTrue(desk.borrow(ama, audioCourse));
        desk.returnItem(audioCourse);
        assertFalse(desk.borrow(ama, novel));
        desk.printEndOfDayReport();

        // Every line matches the original program except line 4, where the original printed
        // Lent "Things Fall Apart" to Ama Owusu for 28 days and corrupted its records.
        String expected = """
                Lent "Things Fall Apart" to Kofi Mensah for 14 days
                Lent "French Audio Course" to Ama Owusu for 14 days
                Returned "French Audio Course"
                Cannot lend "Things Fall Apart" to Ama Owusu: it is already on loan to Kofi Mensah

                === The Reading Room - end of day ===
                Items:
                  "Things Fall Apart" (BOOK): ON_LOAN - with Kofi Mensah
                  "French Audio Course" (AUDIO_KIT): AVAILABLE
                Active loans:
                  Loan #1: "Things Fall Apart" -> Kofi Mensah
                """;
        assertEquals(expected, consoleText());
    }

    @Test
    @DisplayName("Returning an item that is not on loan prints Returned, exactly as the original did")
    void returnOfItemNotOnLoanMatchesOriginal() {
        Book novel = new Book("B-0001", "Things Fall Apart");
        library.addItem(novel);

        desk.returnItem(novel);
        desk.printEndOfDayReport();

        String expected = """
                Returned "Things Fall Apart"

                === The Reading Room - end of day ===
                Items:
                  "Things Fall Apart" (BOOK): AVAILABLE
                Active loans:
                """;
        assertEquals(expected, consoleText());
    }

    /** Console output with Windows line endings turned into "\n", so the test passes on any OS. */
    private String consoleText() {
        return console.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
