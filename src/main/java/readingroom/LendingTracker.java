package readingroom;
// The Reading Room - community library lending tracker
// Original program, built by a volunteer. New volunteers are told not to touch it.
// Run it first to see the sort of thing they are worried about, and look closely
// at the items and active loans in the end-of-day report.

import java.util.*;
import readingroom.member.Member;
import readingroom.member.MemberType;
import readingroom.item.LendingItem;
import readingroom.library.Library;

public final class LendingTracker {

    private LendingTracker() {
        // This class is not meant to be instantiated.
    }

    static void main(String[] args) {
        Library lib = new Library("The Reading Room");

        LendingItem novel = new LendingItem("Things Fall Apart", "BOOK");
        LendingItem audioCourse = new LendingItem("French Audio Course", "AUDIO_KIT");
        lib.addItem(novel);
        lib.addItem(audioCourse);

        Member kofi = new Member("Kofi Mensah", MemberType.STANDARD);
        Member ama = new Member("Ama Owusu", MemberType.STUDENT);
        lib.addMember(kofi);
        lib.addMember(ama);

        // a normal afternoon
        lib.borrow(kofi, novel);
        lib.borrow(ama, audioCourse);
        lib.returnItem(audioCourse);

        // ... and the sort of thing that worries them:
        lib.borrow(ama, novel); // the novel is already out with Kofi, but nothing stops this

        lib.printReport();
    }
}

