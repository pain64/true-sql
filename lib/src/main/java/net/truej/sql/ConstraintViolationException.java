package net.truej.sql;

import org.jetbrains.annotations.Nullable;

public class ConstraintViolationException extends RuntimeException {
    // Nulls here are for 'bad jdbc drivers'
    private final @Nullable String catalogName;
    private final @Nullable String schemaName;
    private final String tableName;
    private final String constraintName;

    public ConstraintViolationException(
        @Nullable String catalogName, @Nullable String schemaName,
        String tableName, String constraintName
    ) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    @SafeVarargs public final <T, E extends Exception> T when(
        Constraint<T, E>... constraints
    ) throws E {
        for (var constraint : constraints) {
            if (
                (
                    constraint.catalogName == null || catalogName == null ||
                    constraint.catalogName.equals(this.catalogName)
                ) && (
                    constraint.schemaName == null || schemaName == null ||
                    constraint.schemaName.equals(this.schemaName)
                )
                && constraint.tableName.equalsIgnoreCase(tableName)
                && constraint.constraintName.equalsIgnoreCase(constraintName)
            )
                return constraint.handler.handle();
        }

        throw this;
    }

    @Override public String toString() {
        return "ConstraintViolationException{" +
               "catalogName='" + catalogName + '\'' +
               ", schemaName='" + schemaName + '\'' +
               ", tableName='" + tableName + '\'' +
               ", constraintName='" + constraintName + '\'' +
               '}';
    }
}
