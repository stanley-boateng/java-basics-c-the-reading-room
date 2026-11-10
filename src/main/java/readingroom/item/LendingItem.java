package readingroom.item;
public class LendingItem {
    public String title;
    public String type;   // "BOOK" or "AUDIO_KIT"
    public String status; // "AVAILABLE" or "ON_LOAN"
    public String currentBorrower;
    public int id;
    static int counter = 1;

    public LendingItem(String title, String type) {
        this.id = counter;
        counter++;
        this.title = title;
        this.type = type;
        this.status = "AVAILABLE";
    }
    
}
