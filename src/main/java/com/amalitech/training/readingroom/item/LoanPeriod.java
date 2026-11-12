package com.amalitech.training.readingroom.item;

import java.util.Objects;

import com.amalitech.training.readingroom.member.MemberType;

public record LoanPeriod(int standardDays, int studentDays) {

    public LoanPeriod {
        if (standardDays <= 0 || studentDays <= 0) {
            throw new IllegalArgumentException("Loan periods must be at least one day");
        }
    }

    public int daysFor(MemberType memberType) {
        Objects.requireNonNull(memberType, "memberType cannot be null");
        // No default branch on purpose: if a new MemberType is added, this stops compiling
        // until someone decides that type's loan length, instead of it silently getting the
        // standard length.
        return switch (memberType) {
            case STANDARD -> standardDays;
            case STUDENT -> studentDays;
        };
    }
}
