package com.truej.sql.v3;

public record Constraint<T, E extends Exception>(
    String tableName, String constraintName, Handler<T, E> handler
) {
    public interface Handler<T, E extends Exception> {
        T handle() throws E;
    }
}
