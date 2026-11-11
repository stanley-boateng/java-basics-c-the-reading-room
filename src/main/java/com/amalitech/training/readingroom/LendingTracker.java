package com.amalitech.training.readingroom;
// The Reading Room - community library lending tracker
// Original program, built by a volunteer. New volunteers are told not to touch it.
// Run it first to see the sort of thing they are worried about, and look closely
// at the items and active loans in the end-of-day report.


import com.amalitech.training.readingroom.member.Member;
import com.amalitech.training.readingroom.member.MemberType;
import com.amalitech.training.readingroom.item.Book;
import com.amalitech.training.readingroom.library.Library;
import com.amalitech.training.readingroom.desk.LendingDesk;
import com.amalitech.training.readingroom.item.AudioKit;



public final class LendingTracker {

    private LendingTracker() {
        // This class is not meant to be instantiated.
    }

    public static void main(String[] args) {
        var library = new Library("The Reading Room");
        var desk = new LendingDesk(library, System.out);

        var novel = new Book("Things Fall Apart");
        var audioCourse = new AudioKit("French Audio Course");
        library.addItem(novel);
        library.addItem(audioCourse);

        var kofi = new Member("Kofi Mensah", MemberType.STANDARD);
        var ama = new Member("Ama Owusu", MemberType.STUDENT);
        library.addMember(kofi);
        library.addMember(ama);

        // a normal afternoon
        desk.borrow(kofi, novel);
        desk.borrow(ama, audioCourse);
        desk .returnItem(audioCourse);

        // ... and the sort of thing that worries them:
        desk.borrow(ama, novel); // the novel is already out with Kofi, but nothing stops this

        desk.printEndOfDayReport();
    }
}

