package readingroom.item;

public final class Book extends LendingItem {

    private static final LoanPeriod LOAN_PERIOD = new LoanPeriod(14, 28);

    public Book(String title) {
        super(title);
    }

    @Override
    public String typeCode() {
        return "BOOK";
    }

    @Override
    public LoanPeriod loanPeriod() {
        return LOAN_PERIOD;
    }
    
}
