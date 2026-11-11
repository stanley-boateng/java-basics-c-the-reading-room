package com.amalitech.training.readingroom.library;

public final class ItemAlreadyOnLoanException extends Exception {
 
    // Every exception can be saved and reloaded ("serialized"), so Java expects a version
    // number. This project never does that; the line just keeps the strict build happy.
    private static final long serialVersionUID = 1L;
 
    private final int blockingLoanNumber;
    private final String borrowerName;
 
    ItemAlreadyOnLoanException(Loan blockingLoan) {
        // Keep the member's name out of the message so the message is safe to log.
        super("\"" + blockingLoan.item().title() + "\" is already on loan (loan #" + blockingLoan.number() + ")");
        this.blockingLoanNumber = blockingLoan.number();
        this.borrowerName = blockingLoan.member().name();
    }
 
    /** Number of the loan that is blocking this one. */
    public int blockingLoanNumber() {
        return blockingLoanNumber;
    }
 
    /** Who currently has the item, for the desk to tell the volunteer. */
    public String borrowerName() {
        return borrowerName;
    }
}
