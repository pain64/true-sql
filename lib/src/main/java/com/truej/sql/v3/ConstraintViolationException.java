package com.truej.sql.v3;

import com.truej.sql.v3.Constraint;

public class ConstraintViolationException extends RuntimeException {
    private final String tableName;
    private final String constraintName;

    public ConstraintViolationException(String tableName, String constraintName) {
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    @SafeVarargs public final <T> T is(Constraint<T>... constraints) {
        throw this;
    }
}
