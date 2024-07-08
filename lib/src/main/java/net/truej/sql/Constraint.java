package net.truej.sql;

import net.truej.sql.source.Source;

public record Constraint<T, E extends Exception>(
    Source source, String tableName, String constraintName, Handler<T, E> handler
) {
    public interface Handler<T, E extends Exception> {
        T handle() throws E;
    }
}
