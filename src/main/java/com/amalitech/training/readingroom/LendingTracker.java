package com.amalitech.training.readingroom;

import com.amalitech.training.readingroom.desk.LendingDesk;
import com.amalitech.training.readingroom.item.AudioKit;
import com.amalitech.training.readingroom.item.Book;
import com.amalitech.training.readingroom.library.Library;
import com.amalitech.training.readingroom.member.Member;
import com.amalitech.training.readingroom.member.MemberType;

/**
 * The Reading Room - community library lending tracker.
 *
 * Replays the same afternoon as the original program. The output is identical,
 * except that lending an item that is already on loan is now refused.
 */
public final class LendingTracker {

    private LendingTracker() {
        // This class is not meant to be instantiated.
    }

    public static void main(String[] args) {
        var library = new Library("The Reading Room");
        var desk = new LendingDesk(library, System.out);
        replayOriginalAfternoon(library, desk);
    }

    /** The same afternoon as the original program's main method. */
    private static void replayOriginalAfternoon(Library library, LendingDesk desk) {
        var novel = new Book("B-0001", "Things Fall Apart");
        var audioCourse = new AudioKit("A-0001", "French Audio Course");
        library.addItem(novel);
        library.addItem(audioCourse);

        var kofi = new Member("M-0001", "Kofi Mensah", MemberType.STANDARD);
        var ama = new Member("M-0002", "Ama Owusu", MemberType.STUDENT);
        library.addMember(kofi);
        library.addMember(ama);

        // a normal afternoon
        desk.borrow(kofi, novel);
        desk.borrow(ama, audioCourse);
        desk.returnItem(audioCourse);

        // the case that used to corrupt the records: the novel is already out with Kofi,
        // so this loan is now refused
        desk.borrow(ama, novel);

        desk.printEndOfDayReport();
    }
}
