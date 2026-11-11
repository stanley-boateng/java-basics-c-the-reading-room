package com.amalitech.training.readingroom.report;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.amalitech.training.readingroom.item.LendingItem;
import com.amalitech.training.readingroom.library.Library;
import com.amalitech.training.readingroom.library.Loan;

public final class EndOfDayReport {
    private EndOfDayReport() {
        // This class is not meant to be instantiated.
    }

    /** Prints the report as of today, preceded by a blank line, exactly as the original program did. */
    public static void printTo(Library library, PrintStream out) {
        Objects.requireNonNull(out, "out cannot be null");
        out.println();
        for (String line : lines(library)) {
            out.println(line);
        }
    }

    /** The report's lines as of the library's today. */
    public static List<String> lines(Library library) {
        Objects.requireNonNull(library, "library cannot be null");
        return lines(library, library.today());
    }

    /** One loan, formatted as in the original report: Loan #1: "Title" -> Member. */
    private static String describe(Loan loan) {
        return "  Loan #" + loan.number() + ": \"" + loan.item().title() + "\" -> " + loan.member().name();
    }

    /** The report's lines as of a given date, for example to see what will be overdue next week. */
    public static List<String> lines(Library library, LocalDate asOfDate) {
        Objects.requireNonNull(library, "library cannot be null");
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");

        List<String> reportLines = new ArrayList<>();
        reportLines.add("=== " + library.name() + " - end of day ===");
        reportLines.add("Items:");
        for (LendingItem item : library.items()) {
            String status = "AVAILABLE";
            if (library.isOnLoan(item)) {
                status = "ON_LOAN - with " + library.activeLoanFor(item).member().name();
            }
            reportLines.add("  \"" + item.title() + "\" (" + item.typeCode() + "): " + status);
        }
        reportLines.add("Active loans:");
        for (Loan loan : library.activeLoans()) {
            reportLines.add(describe(loan));
        }

        List<Loan> overdue = library.overdueLoans(asOfDate);
        if (!overdue.isEmpty()) {
            reportLines.add("Overdue loans:");
            for (Loan loan : overdue) {
                long daysLate = ChronoUnit.DAYS.between(loan.dueOn(), asOfDate);
                String unit = daysLate == 1 ? "day" : "days";
                reportLines.add(describe(loan) + " (due " + loan.dueOn() + ", " + daysLate + " " + unit + " late)");
            }
        }
        return reportLines;
    }
}
