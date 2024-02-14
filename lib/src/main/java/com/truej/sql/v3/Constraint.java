package com.truej.sql.v3;

import java.util.function.Supplier;

public class Constraint<T> {
    final String tableName;
    final String constraintName;
    final Supplier<T> handler;

    public Constraint(String tableName, String constraintName, Supplier<T> handler) {
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.handler = handler;
    }
}
