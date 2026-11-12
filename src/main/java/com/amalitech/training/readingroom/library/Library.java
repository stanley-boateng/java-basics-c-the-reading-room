package com.amalitech.training.readingroom.library;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.amalitech.training.readingroom.item.LendingItem;
import com.amalitech.training.readingroom.member.Member;

public final class Library {
    private final String name;
    private final Clock clock;

    private final List<LendingItem> items = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    // Every loan ever made, open or closed, in the order they were made.
    private final List<Loan> loans = new ArrayList<>();
    private final Map<LendingItem, Integer> activeLoanIndexByItem = new HashMap<>();

    public Library(String name) {
        this(name, Clock.systemDefaultZone());
    }

    public Library(String name, Clock clock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Library name cannot be blank");
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

    public void addItem(LendingItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (items.contains(item)) {
            throw new IllegalArgumentException("Copy " + item.copyId() + " is already registered");
        }
        items.add(item);
    }

    public void addMember(Member member) {
        Objects.requireNonNull(member, "member cannot be null");
        if (members.contains(member)) {
            throw new IllegalArgumentException("Member " + member.memberId() + " is already registered");
        }
        members.add(member);
    }

    private void requireRegisteredMember(Member member) {
        Objects.requireNonNull(member, "member cannot be null");
        if (!members.contains(member)) {
            throw new IllegalArgumentException("Member " + member.memberId() + " is not registered with " + name);
        }
    }

    private void requireRegisteredItem(LendingItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!items.contains(item)) {
            throw new IllegalArgumentException("Copy " + item.copyId() + " is not registered with " + name);
        }
    }

    /**
     * Lends an item to a member. The loan period depends on the kind of item and member.
     *
     * @throws ItemAlreadyOnLoanException if the item is already out; nothing is changed
     */
    public Loan borrow(Member member, LendingItem item) throws ItemAlreadyOnLoanException {
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

    /** Closes the item's active loan. Returns false, changing nothing, if it was not on loan. */
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

    public Loan activeLoanFor(LendingItem item) {
        requireRegisteredItem(item);
        Integer index = activeLoanIndexByItem.get(item);
        if (index == null) {
            throw new IllegalStateException("Copy " + item.copyId() + " is not currently on loan");
        }
        return loans.get(index);
    }

    // The list methods below return read-only copies, so callers cannot change the records.

    /** Every registered item, in the order it was added. */
    public List<LendingItem> items() {
        return List.copyOf(items);
    }

    /** Every registered member, in the order they joined. */
    public List<Member> members() {
        return List.copyOf(members);
    }

    /** Loans that have not been returned yet. */
    public List<Loan> activeLoans() {
        List<Loan> active = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isActive()) {
                active.add(loan);
            }
        }
        return List.copyOf(active);
    }

    /** Active loans that are past their due date on the given date. */
    public List<Loan> overdueLoans(LocalDate asOfDate) {
        Objects.requireNonNull(asOfDate, "asOfDate cannot be null");
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOverdueOn(asOfDate)) {
                overdue.add(loan);
            }
        }
        return List.copyOf(overdue);
    }

    /** Every loan the member has made, open or closed. */
    public List<Loan> loansFor(Member member) {
        requireRegisteredMember(member);
        List<Loan> history = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.member().equals(member)) {
                history.add(loan);
            }
        }
        return List.copyOf(history);
    }
}
