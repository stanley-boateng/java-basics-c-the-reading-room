package readingroom.library;
import java.util.ArrayList;
import java.util.List;

import member.Member;
import readingroom.item.LendingItem;

public class Library {
    public String name;
    public List<LendingItem> items = new ArrayList<>();
    public List<Member> members = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    public Library(String name) {
        this.name = name;
    }

    public void addItem(LendingItem i) {
        items.add(i);
    }

    public void addMember(Member m) {
        members.add(m);
    }

    // Lend an item to a member. The loan period depends on the kind of item and member.
    public void borrow(Member m, LendingItem item) {
        int loanDays = 0;
        if (item.type.equals("BOOK")) {
            loanDays = 14;
            if (m.kind.equals("STUDENT")) {
                loanDays = 28; // students get longer loans
            }
            item.status = "ON_LOAN";
            item.currentBorrower = m.name;
            System.out.println("Lent \"" + item.title + "\" to " + m.name + " for " + loanDays + " days");
        } else if (item.type.equals("AUDIO_KIT")) {
            loanDays = 7;
            if (m.kind.equals("STUDENT")) {
                loanDays = 14; // students get longer loans
            }
            item.status = "ON_LOAN";
            item.currentBorrower = m.name;
            System.out.println("Lent \"" + item.title + "\" to " + m.name + " for " + loanDays + " days");
        }
        m.history.add("Borrowed " + item.title);
        loans.add(new Loan(m.name, item.title, loanDays));
    }

    public void returnItem(LendingItem item) {
        item.status = "AVAILABLE";
        item.currentBorrower = null;
        for (Loan l : loans) {
            if (l.itemTitle.equals(item.title) && l.status.equals("ACTIVE")) {
                l.status = "RETURNED";
            }
        }
        System.out.println("Returned \"" + item.title + "\"");
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public void printReport() {
        System.out.println();
        System.out.println("=== " + name + " - end of day ===");
        System.out.println("Items:");
        for (LendingItem i : items) {
            System.out.println("  \"" + i.title + "\" (" + i.type + "): " + i.status
                    + (i.currentBorrower != null ? " - with " + i.currentBorrower : ""));
        }
        System.out.println("Active loans:");
        for (Loan l : loans) {
            if (l.status.equals("ACTIVE")) {
                System.out.println("  Loan #" + l.id + ": \"" + l.itemTitle + "\" -> " + l.memberName);
            }
        }
    }
    
}
