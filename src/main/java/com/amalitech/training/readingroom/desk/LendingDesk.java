package com.amalitech.training.readingroom.desk;

import java.io.PrintStream;
import java.util.Objects;

import com.amalitech.training.readingroom.library.Library;
import com.amalitech.training.readingroom.library.Loan;
import com.amalitech.training.readingroom.library.ItemAlreadyOnLoanException;
import com.amalitech.training.readingroom.item.LendingItem;
import com.amalitech.training.readingroom.member.Member;
import com.amalitech.training.readingroom.report.EndOfDayReport;


public final class LendingDesk {
    private final Library library;
    private final PrintStream out;

    public LendingDesk(Library library, PrintStream out) {
        this.library = Objects.requireNonNull(library, "library cannot be null");
        this.out = Objects.requireNonNull(out, "out cannot be null");
    }

    public boolean borrow(Member member, LendingItem item) {
        try {
            Loan loan = library.borrow(member, item);
            out.println("Lent \"" + item.title() + "\" to " + member.name() + " for " + loan.loanDays() + " days");
            return true;    
        } catch (ItemAlreadyOnLoanException e) {
            out.println("Cannot lend \"" + item.title() + "\" to " + member.name() + ": it is already on loan to " + e.borrowerName());
            return false;
        }

    }

    public void returnItem(LendingItem item){
        library.returnItem(item);
        out.println("Returned \"" + item.title() + "\"");
    }

    public void printEndOfDayReport() {
        out.println();
        for (String line : EndOfDayReport.lines(library)) {
            out.println(line);
        }
    }


}
