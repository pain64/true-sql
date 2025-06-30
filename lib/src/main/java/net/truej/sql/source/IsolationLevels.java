package net.truej.sql.source;

import java.sql.Connection;

public enum IsolationLevels implements IsolationLevel {
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    final int jdbcCode;

    IsolationLevels(int jdbcCode) {
        this.jdbcCode = jdbcCode;
    }

    @Override public int jdbcCode() {
        return jdbcCode;
    }
}
