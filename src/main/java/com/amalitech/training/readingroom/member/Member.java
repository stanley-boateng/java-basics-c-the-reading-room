package com.amalitech.training.readingroom.member;

import java.util.Objects;

/**
 * A registered person. The member ID (the number on their library card, such as "M-0001")
 * is what makes two members the same person or different people: two members can share a
 * name. The ID is used inside the program only; the console output shows names, as the
 * original program did.
 */
public final class Member {
    private final String memberId;
    private final String name;
    private final MemberType type;

    public Member(String memberId, String name, MemberType type) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalArgumentException("Member ID cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Member name cannot be blank");
        }
        this.memberId = memberId;
        this.name = name;
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }

    public String memberId() {
        return memberId;
    }

    public String name() {
        return name;
    }

    public MemberType type() {
        return type;
    }

    @Override
    public String toString() {
        // No name here: toString() ends up in logs and test failure messages, and a name is personal data.
        return "Member[" + memberId + "," + type + "]";
    }

    /** Two member objects are the same person if and only if their IDs match */
    @Override
    public boolean equals(Object other) {
        return other instanceof Member m && memberId.equals(m.memberId);
    }

    @Override
    public int hashCode() {
        return memberId.hashCode();
    }
}
