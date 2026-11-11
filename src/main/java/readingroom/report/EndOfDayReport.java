package readingroom.report;

import readingroom.library.Library;
import readingroom.item.LendingItem;
import readingroom.library.Loan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.time.temporal.ChronoUnit;

public final class EndOfDayReport {
    private EndOfDayReport() {
        // This class is not meant to be instantiated.
    }

    // Return a list of strings representing the lines of an end-of-day report for the given library, as of the given date.    
    public static List<String> lines(Library library){
        Objects.requireNonNull("library cannot be empty");
        return lines(library, library.today());
    }

    //
    private static String describe(Loan loan) {
        return "  Loan #" + loan.number() + ": \"" + loan.item().title() + "\" -> " + loan.member().name();
    }

    
    public static List<String> lines(Library library, LocalDate asOfDate) {
        Objects.requireNonNull(library, "library cannot be null");
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");

        List<String> reportLines = new ArrayList<>();
        reportLines.add("=== " + library.name() + " - end of day ===");
        reportLines.add("Items:");
        for (LendingItem item : library.items()) {
            String status = "AVAILABLE";
            if (library.isOnLoan(item)){
                status = "ON_LOAN - with " + library.getActiveLoan(item).member().name(); 
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
