package com.truej.sql.v3.prepare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

interface With<S, P extends PreparedStatement> {
    interface StatementConstructor<S extends PreparedStatement> {
        S construct(Connection connection, String sql) throws SQLException;
    }

    S with(StatementConstructor<P> constructor);

    interface GeneratedKeysConstructors<S> extends With<S, PreparedStatement> {

        default S withGeneratedKeys() {
            return with((connection, sql) ->
                connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            );
        }

        default S withGeneratedKeys(int... columnIndexes) {
            return with((connection, sql) ->
                connection.prepareStatement(sql, columnIndexes)
            );
        }

        default S withGeneratedKeys(String... columnNames) {
            return with((connection, sql) ->
                connection.prepareStatement(sql, columnNames)
            );
        }
    }
}
