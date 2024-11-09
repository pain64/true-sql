package net.truej.sql.fetch;

public interface NewConstraint {

    default <T, E extends Exception> Constraint<T, E> constraint(
        String catalogName, String schemaName,
        String tableName, String constraintName, Constraint.Handler<T, E> handler
    ) {
        return new Constraint<>(catalogName, schemaName, tableName, constraintName, handler);
    }

    default <T, E extends Exception> Constraint<T, E> constraint(
        String schemaName, String tableName, String constraintName, Constraint.Handler<T, E> handler
    ) {
        return constraint(null, schemaName, tableName, constraintName, handler);
    }

    default <T, E extends Exception> Constraint<T, E> constraint(
        String tableName, String constraintName, Constraint.Handler<T, E> handler
    ) {
        return constraint(null, null, tableName, constraintName, handler);
    }
}
