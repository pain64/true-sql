package net.truej.sql;

import net.truej.sql.source.Source;
import org.jetbrains.annotations.Nullable;

public class Constraint<T, E extends Exception> {
    public interface Handler<T, E extends Exception> {
        T handle() throws E;
    }

    public final @Nullable String catalogName;
    public final @Nullable String schemaName;
    public final String tableName;
    public final String constraintName;
    public final Handler<T, E> handler;

    public Constraint(
        Source source, String catalogName, String schemaName,
        String tableName, String constraintName, Handler<T, E> handler
    ) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.handler = handler;
    }

    public Constraint(
        Source source, String schemaName, String tableName,
        String constraintName, Handler<T, E> handler
    ) {
        this.catalogName = null;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.handler = handler;
    }

    public Constraint(
        Source source, String tableName, String constraintName, Handler<T, E> handler
    ) {
        this.catalogName = null;
        this.schemaName = null;
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.handler = handler;
    }
}
