package com.amalitech.training.readingroom.report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.amalitech.training.readingroom.item.AudioKit;
import com.amalitech.training.readingroom.item.Book;
import com.amalitech.training.readingroom.library.ItemAlreadyOnLoanException;
import com.amalitech.training.readingroom.library.Library;
import com.amalitech.training.readingroom.member.Member;
import com.amalitech.training.readingroom.member.MemberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The report format, and the overdue section added beyond the brief. */
class EndOfDayReportTest {

    private static final LocalDate BORROW_DAY = LocalDate.of(2026, 11, 11);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneOffset.UTC);

    private Library library;
    private Book novel;

    @BeforeEach
    void setUp() throws ItemAlreadyOnLoanException {
        library = new Library("The Reading Room", FIXED_CLOCK);
        novel = new Book("B-0001", "Things Fall Apart");
        AudioKit audioCourse = new AudioKit("A-0001", "French Audio Course");
        Member kofi = new Member("M-0001", "Kofi Mensah", MemberType.STANDARD);
        Member ama = new Member("M-0002", "Ama Owusu", MemberType.STUDENT);
        library.addItem(novel);
        library.addItem(audioCourse);
        library.addMember(kofi);
        library.addMember(ama);
        library.borrow(kofi, novel);        // loan #1: 14 days, due 2026-11-25
        library.borrow(ama, audioCourse);   // loan #2: 14 days (student), due 2026-11-25
    }

    @Test
    @DisplayName("On the due date nothing is overdue and the report is in the original format")
    void noOverdueSectionOnDueDate() {
        String expected = """
                === The Reading Room - end of day ===
                Items:
                  "Things Fall Apart" (BOOK): ON_LOAN - with Kofi Mensah
                  "French Audio Course" (AUDIO_KIT): ON_LOAN - with Ama Owusu
                Active loans:
                  Loan #1: "Things Fall Apart" -> Kofi Mensah
                  Loan #2: "French Audio Course" -> Ama Owusu
                """;

        assertEquals(expected, asText(EndOfDayReport.lines(library, BORROW_DAY.plusDays(14))));
    }

    @Test
    @DisplayName("Loans past their due date are listed with how many days late they are")
    void overdueLoansAreListed() {
        String expected = """
                === The Reading Room - end of day ===
                Items:
                  "Things Fall Apart" (BOOK): ON_LOAN - with Kofi Mensah
                  "French Audio Course" (AUDIO_KIT): ON_LOAN - with Ama Owusu
                Active loans:
                  Loan #1: "Things Fall Apart" -> Kofi Mensah
                  Loan #2: "French Audio Course" -> Ama Owusu
                Overdue loans:
                  Loan #1: "Things Fall Apart" -> Kofi Mensah (due 2026-11-25, 3 days late)
                  Loan #2: "French Audio Course" -> Ama Owusu (due 2026-11-25, 3 days late)
                """;

        assertEquals(expected, asText(EndOfDayReport.lines(library, BORROW_DAY.plusDays(17))));
    }

    @Test
    @DisplayName("Returned items are never overdue, and one day late uses the singular")
    void returnedLoansAreNotOverdue() {
        library.returnItem(novel);

        List<String> lines = EndOfDayReport.lines(library, BORROW_DAY.plusDays(15));

        assertEquals(List.of(
                "Overdue loans:",
                "  Loan #2: \"French Audio Course\" -> Ama Owusu (due 2026-11-25, 1 day late)"),
                lines.subList(lines.size() - 2, lines.size()));
    }

    /** Joins report lines the way a text block is written: one per line, ending in a newline. */
    private static String asText(List<String> lines) {
        return String.join("\n", lines) + "\n";
    }
}
