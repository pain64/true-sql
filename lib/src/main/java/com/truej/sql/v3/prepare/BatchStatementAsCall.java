package com.truej.sql.v3.prepare;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

abstract public class BatchStatementAsCall extends Base.Batch<BatchStatementAsCall, CallableStatement> {

    @Override CallableStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareCall(sql);
    }

    @Override BatchStatementAsCall self() { return this; }

    @Override long[] execute(CallableStatement stmt) throws SQLException {
        return stmt.executeLargeBatch();
    }
}