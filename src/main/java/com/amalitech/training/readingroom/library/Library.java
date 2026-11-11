package com.amalitech.training.readingroom.library;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.amalitech.training.readingroom.member.Member;
import com.amalitech.training.readingroom.item.LendingItem;

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

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public void addItem(LendingItem item){
        Objects.requireNonNull(item,"item cannot be null");
        if (items.contains(item)){
            throw new IllegalArgumentException(item + " is already registered");
        }
        items.add(item);
    }

    public void addMember(Member member){
        Objects.requireNonNull(member, "member must not be null");
        if (members.contains(member)){
            throw new IllegalArgumentException(member + " is already registered");
        }
        members.add(member);
    }

    private void requireRegisteredMember(Member member){
        Objects.requireNonNull(member, "Member cannot be null");
        if (!members.contains(member)){
            throw new IllegalArgumentException(member + " is not registered with " + name);
        }
    }

    private void requireRegisteredItem(LendingItem item){
        Objects.requireNonNull(item, "Item cannot not be null");
        if (!items.contains(item)){
            throw new IllegalArgumentException(item + " is not registered with " + name);
        }
    }

    // Lend an item to a member. The loan period depends on the kind of item and member.
    public Loan borrow(Member member, LendingItem item) throws ItemAlreadyOnLoanException{
        requireRegisteredMember(member);
        requireRegisteredItem(item);

        Integer activeLoanIndex = activeLoanIndexByItem.get(item);
        if (activeLoanIndex != null) {
            throw new ItemAlreadyOnLoanException(loans.get(activeLoanIndex));
        }

        var loan = Loan.open(loans.size() + 1, item, member, today());
        loans.add(loan);
        activeLoanIndexByItem.put(item, loans.size() - 1);
        return loan;
    }

    public boolean returnItem(LendingItem item) {
        requireRegisteredItem(item);

        Integer index = activeLoanIndexByItem.remove(item);
        if (index == null) {
            return false;
        }
        loans.set(index, loans.get(index).close(today()));
        return true;
    }

    public boolean isOnLoan(LendingItem item) {
        requireRegisteredItem(item);
        return activeLoanIndexByItem.containsKey(item);
    }
    
    public Loan getActiveLoan(LendingItem item) {
        requireRegisteredItem(item);
        Integer index = activeLoanIndexByItem.get(item);
        if (index == null) {
            throw new IllegalStateException(item + " is not currently on loan");
        }
        return loans.get(index);
    }

    // Return a copy of the list of all items in the library, so that the caller cannot modify the library's internal state.
    public List<LendingItem> items() {
        return List.copyOf(items);
    }

    // Return a copy of the list of all members in the library, so that the caller cannot modify the library's internal state.
    public List<Member> members() {
        return List.copyOf(members);
    }

    // Return a copy of the list of all loans that are currently active, so that the caller cannot modify the library's internal state.
    public List<Loan> activeLoans() {
        List<Loan> active = new ArrayList<>();
        for (Loan loan : loans){
            if (loan.isActive()){
                active.add(loan);
            }
        }
        return List.copyOf(active);
    }

    // Return a copy of the list of all loans that are overdue as of the given date, so that the caller cannot modify the library's internal state.
    public List<Loan> overdueLoans(LocalDate asOfDate) {
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans){
            if (loan.isOverdueOn(asOfDate)){
                overdue.add(loan);
            }
        }
        return List.copyOf(overdue);
    }

    // Return a copy of the list of all loans for the given member, so that the caller cannot modify the library's internal state.
    public List<Loan> loansFor(Member member){
        requireRegisteredMember(member);
        List<Loan> history = new ArrayList<>();
        for (Loan loan : loans){
            if (loan.member().equals(member)){
                history.add(loan);
            }
        }
        return List.copyOf(history);
    }
    
}
