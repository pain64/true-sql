package com.truej.sql.v3.prepare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BatchStatementAsGeneratedKeys extends Base.Batch<BatchStatementAsGeneratedKeys, PreparedStatement> {

    @Override PreparedStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override BatchStatementAsGeneratedKeys self() { return this; }

    @Override long[] execute(PreparedStatement stmt) throws SQLException {
        return stmt.executeLargeBatch();
    }
}
