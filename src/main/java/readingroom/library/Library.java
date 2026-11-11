package readingroom.library;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import readingroom.member.Member;
import readingroom.item.LendingItem;

public final class Library {
    private final String name;
    private final Clock clock;


    private final List<LendingItem> items = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    //every loan ever made
    private final List<Loan> loans = new ArrayList<>();
    private final Map<LendingItem, Integer> activeLoanIndexByItem = new HashMap<>();

    public Library(String name) {
        this(name, Clock.systemDefaultZone());
    }

    public Library(String name, Clock clock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Library name must not be blank");
        }
        this.name = name;
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    public String name() {
        return name;
    }

    public LocalDate today() {{
        return LocalDate.now(clock);
    }}

    public void addItem(LendingItem item){
        Objects.requireNonNull(item,"item cannot be null");
        if (items.contains(item)){
            throw new IllegalArgumentException(item + " is already registered");
        }
        items.add(item);
    }

    public void addMember(Member member){
        Objects.requireNonNull(member, "member must not be null");
        if (items.contains(member)){
            throw new IllegalArgumentException(member + " is already registered");
        }
        members.add(member);
    }

    // Lend an item to a member. The loan period depends on the kind of item and member.
    public Loan borrow(Member member, LendingItem item) throws ItemAlreadyOnLoanException{
    
        private void requireRegistered(Member member){
            Objects.requireNonNull(member, "Member cannot be null");
            if (!members.contains(member)){
                throw new IllegalArgumentException(member + " is not registered with " + name);
            }
        }

        private void requireRegistered(LendingItem item){
            Objects.requireNonNull(item, "Item nust not be null");
            if (!items.contains(item)){
                throw new IllegalArgumentException(item + " is not registered with " + name);
            }
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
