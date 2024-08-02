package net.truej.sql.dsl;

import org.jetbrains.annotations.Nullable;

public class Constraint<T, E extends Exception> {

    public interface Handler<T, E extends Exception> {
        T handle() throws E;
    }

    final @Nullable String catalogName;
    final @Nullable String schemaName;
    final String tableName;
    final String constraintName;
    final Handler<T, E> handler;

    Constraint(
        @Nullable String catalogName, @Nullable String schemaName,
        String tableName, String constraintName, Handler<T, E> handler
    ) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.handler = handler;
    }
}
