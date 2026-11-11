package readingroom.library;

import java.time.LocalDate;
import java.util.Objects;

import readingroom.item.LendingItem;
import readingroom.member.Member;

public record Loan (int number, LendingItem item, Member member, LocalDate borrowedOn, int loanDays, LocalDate returnedOn) {
    

    public Loan {
        if (number <= 0) {
            throw new IllegalArgumentException("Loan number must be positive");
        }
        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(member, "member cannot be null");
        Objects.requireNonNull(borrowedOn, "borrowedOn cannot be null");
        if (loanDays <= 0){
            throw new IllegalArgumentException("Loan length must be at least 1 day");
        }
        if (returnedOn != null && returnedOn.isBefore(borrowedOn)){
            throw new IllegalArgumentException("A loaned book cannot be returned before it started");
        }
    }
    static Loan open(int number, LendingItem item, Member member, LocalDate today) {
        int days = item.loanPeriod().daysFor(member.type());
        return new Loan(number, item, member, today, days, null);
    }

    public boolean isActive(){
        return returnedOn == null;
    }
    public LocalDate dueOn(){
        return borrowedOn.plusDays(loanDays);
    }

    Loan close(LocalDate today){
        if (!isActive()) {
            throw new IllegalArgumentException("Loan #" + number + " is already closed");
        }
        return new Loan(number, item, member, borrowedOn, loanDays, today);
    }
    
    public boolean isOverdueOn(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        return isActive() && date.isAfter(dueOn());
    }
    
}
