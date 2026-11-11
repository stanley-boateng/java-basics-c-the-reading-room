package com.amalitech.training.readingroom.library;

import java.time.LocalDate;
import java.util.Objects;

import com.amalitech.training.readingroom.item.LendingItem;
import com.amalitech.training.readingroom.member.Member;

/** One loan. Never changed after it is created: closing a loan makes a new record. */
public record Loan(int number, LendingItem item, Member member, LocalDate borrowedOn, int loanDays,
        LocalDate returnedOn) {

    public Loan {
        if (number <= 0) {
            throw new IllegalArgumentException("Loan number must be positive");
        }
        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(member, "member cannot be null");
        Objects.requireNonNull(borrowedOn, "borrowedOn cannot be null");
        if (loanDays <= 0) {
            throw new IllegalArgumentException("Loan length must be at least one day");
        }
        if (returnedOn != null && returnedOn.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("A loan cannot be returned before it started");
        }
    }

    /** Opens a loan starting today, with the length set by the item's kind and the member's type. */
    static Loan open(int number, LendingItem item, Member member, LocalDate today) {
        int days = item.loanPeriod().daysFor(member.type());
        return new Loan(number, item, member, today, days, null);
    }

    public boolean isActive() {
        return returnedOn == null;
    }

    /** The date the item is due back. */
    public LocalDate dueOn() {
        return borrowedOn.plusDays(loanDays);
    }

    /** A copy of this loan marked as returned on the given date. Fails if it is already closed. */
    Loan close(LocalDate today) {
        if (!isActive()) {
            throw new IllegalStateException("Loan #" + number + " is already closed");
        }
        return new Loan(number, item, member, borrowedOn, loanDays, today);
    }

    /** True if the loan is still out and the given date is after its due date. */
    public boolean isOverdueOn(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        return isActive() && date.isAfter(dueOn());
    }
}
