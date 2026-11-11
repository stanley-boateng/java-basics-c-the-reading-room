package readingroom.item;

public class AudioKit extends LendingItem{
    private static final LoanPeriod LOAN_PERIOD = new LoanPeriod(7, 24);

    public AudioKit(String title) {
        super(title);
    }

    @Override 
    public String typeCode() {
        return "AUDIO_KIT";
    }

    @Override 
    public LoanPeriod loanPeriod(){
        return LOAN_PERIOD;
    }
}
