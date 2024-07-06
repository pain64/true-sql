package net.truej.sql;

public class ConstraintViolationException extends RuntimeException {
    private final String tableName;
    private final String constraintName;

    public ConstraintViolationException(String tableName, String constraintName) {
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    @SafeVarargs public final <T, E extends Exception> T when(
        Constraint<T, E>... constraints
    ) throws E {
        for (var constraint : constraints) {
            if (
                constraint.tableName().equalsIgnoreCase(tableName) &&
                constraint.constraintName().equalsIgnoreCase(constraintName)
            )
                return constraint.handler().handle();
        }

        throw this;
    }
}
