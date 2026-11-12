package com.amalitech.training.readingroom.item;

/**
 * One physical copy that can be lent. A library can own several copies of the same title,
 * so each copy carries its own copy ID (the barcode on the item), and the copy ID is what
 * makes two items the same or different. Never compare items by title.
 */
public abstract class LendingItem {

    private final String copyId;
    private final String title;

    protected LendingItem(String copyId, String title) {
        if (copyId == null || copyId.isBlank()) {
            throw new IllegalArgumentException("Copy ID cannot be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        this.copyId = copyId;
        this.title = title;
    }

    /** The barcode on this copy, for example "B-0001". Unique within a library. */
    public final String copyId() {
        return copyId;
    }

    public final String title() {
        return title;
    }

    public abstract String typeCode();

    public abstract LoanPeriod loanPeriod();

    // Two item objects are the same copy if and only if their copy IDs match. Both methods
    // are final so that no subclass can compare copies any other way (by title, for example).

    @Override
    public final boolean equals(Object other) {
        return other instanceof LendingItem otherItem && copyId.equals(otherItem.copyId);
    }

    @Override
    public final int hashCode() {
        return copyId.hashCode();
    }

    @Override
    public String toString() {
        return typeCode() + "[" + copyId + ",\"" + title + "\"]";
    }
}
