package com.truej.sql.v3;

public class ConstraintViolationException extends RuntimeException {
    private final String tableName;
    private final String constraintName;

    public ConstraintViolationException(String tableName, String constraintName) {
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    @SafeVarargs public final <T, E extends Exception> T when(Constraint<T, E>... constraints) {
        throw this;
    }
}
