package readingroom.item;
public abstract class LendingItem {

    private final String title;

    protected LendingItem(String title){
        if (title == null || title.isBlank()){
            throw new IllegalArgumentException("Title cannot be blank");
        }
        this.title = title;
    }

    public final String title(){
        return title;
    }

    public abstract String typeCode();

    public abstract LoanPeriod loanPeriod();

    // public String title;
    // public String type;   // "BOOK" or "AUDIO_KIT"
    // public String status; // "AVAILABLE" or "ON_LOAN"
    // public String currentBorrower;
    // public int id;
    // static int counter = 1;

    // public LendingItem(String title, String type) {
    //     this.id = counter;
    //     counter++;
    //     this.title = title;
    //     this.type = type;
    //     this.status = "AVAILABLE";
    // }
    
}
