package com.amalitech.training.readingroom.item;

public final class AudioKit extends LendingItem {
    private static final LoanPeriod LOAN_PERIOD = new LoanPeriod(7, 14);

    public AudioKit(String copyId, String title) {
        super(copyId, title);
    }

    @Override
    public String typeCode() {
        return "AUDIO_KIT";
    }

    @Override
    public LoanPeriod loanPeriod() {
        return LOAN_PERIOD;
    }
}
