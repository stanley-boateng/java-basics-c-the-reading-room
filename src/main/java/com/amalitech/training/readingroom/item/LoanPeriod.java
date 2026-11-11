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
        if (memberType == MemberType.STUDENT) {
            return studentDays;
        }
        return standardDays;
    }
}
