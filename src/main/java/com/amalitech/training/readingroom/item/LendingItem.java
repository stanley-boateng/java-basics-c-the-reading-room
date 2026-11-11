package com.amalitech.training.readingroom.item;

public abstract class LendingItem {

    private final String title;

    protected LendingItem(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        this.title = title;
    }

    public final String title() {
        return title;
    }

    public abstract String typeCode();

    public abstract LoanPeriod loanPeriod();
}
