package com.truej.sql.v3.prepare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

abstract class StatementSettings<T> extends Settings<T, PreparedStatement> {

    @Override PreparedStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public T withGeneratedKeys() {
        this.constructor = (connection, sql) ->
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        return self();
    }

    public T withGeneratedKeys(int[] columnIndexes) {
        this.constructor = (connection, sql) ->
            connection.prepareStatement(sql, columnIndexes);
        return self();
    }

    public T withGeneratedKeys(String[] columnNames) {
        this.constructor = (connection, sql) ->
            connection.prepareStatement(sql, columnNames);
        return self();
    }
}
