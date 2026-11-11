package com.amalitech.training.readingroom.member;

import java.util.Objects;

public final class Member {
    private final String name;
    private final MemberType type;

    public Member(String name, MemberType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Member name cannot be blank");
        }
        this.name = name;
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }

    public String name() {
        return name;
    }

    public MemberType type() {
        return type;
    }

    @Override
    public String toString() {
        return "Member[" + name + "," + type + "]";
    }
}
