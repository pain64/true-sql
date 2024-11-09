package net.truej.sql.fetch;

import org.jetbrains.annotations.Nullable;

public class ConstraintViolationException extends RuntimeException {
    // Nulls here are for 'bad jdbc drivers'
    private final @Nullable String catalogName;
    private final @Nullable String schemaName;
    private final @Nullable String tableName;
    private final @Nullable String constraintName;

    public ConstraintViolationException(
        @Nullable String catalogName, @Nullable String schemaName,
        @Nullable String tableName, @Nullable String constraintName
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
                    catalogName == null || constraint.catalogName == null ||
                    constraint.catalogName.equalsIgnoreCase(this.catalogName)
                ) && (
                    schemaName == null || constraint.schemaName == null ||
                    constraint.schemaName.equalsIgnoreCase(this.schemaName)
                ) && (
                    tableName == null ||
                    constraint.tableName.equalsIgnoreCase(tableName)
                ) && (
                    constraintName == null ||
                    constraint.constraintName.equalsIgnoreCase(constraintName)
                )
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
