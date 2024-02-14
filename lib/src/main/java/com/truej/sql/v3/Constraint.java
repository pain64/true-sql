package com.truej.sql.v3;

public class Constraint<T, E extends Exception> {

    public interface Handler<T, E extends Exception> {
        T handle() throws E;
    }

    final String tableName;
    final String constraintName;
    final Handler<T, E> handler;

    public Constraint(String tableName, String constraintName, Handler<T, E> handler) {
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.handler = handler;
    }
}
