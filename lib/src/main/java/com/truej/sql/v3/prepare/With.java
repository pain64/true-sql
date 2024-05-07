package com.truej.sql.v3.prepare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

interface With<S, P extends PreparedStatement> {
    interface StatementConstructor<P extends PreparedStatement> {
        boolean hasGeneratedKeys();
        P construct(Connection connection, String sql) throws SQLException;
    }

    interface GeneratedKeysConstructor extends
        StatementConstructor<PreparedStatement> {
        @Override default boolean hasGeneratedKeys() { return true; }
    }

    S with(StatementConstructor<P> constructor);

    interface GeneratedKeysConstructors<S> extends With<S, PreparedStatement> {

        default S asGeneratedKeys(int... columnNumbers) {
            return with((GeneratedKeysConstructor)(connection, sql) ->
                connection.prepareStatement(sql, columnNumbers)
            );
        }

        default S asGeneratedKeys(String... columnNames) {
            return with((GeneratedKeysConstructor)(connection, sql) ->
                connection.prepareStatement(sql, columnNames)
            );
        }
    }
}
