package com.truej.sql.v3.prepare;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class BatchCall extends Base.Batch<BatchCall, CallableStatement> {

    @Override CallableStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareCall(sql);
    }

    @Override BatchCall self() { return this; }

    @Override long[] execute(CallableStatement stmt) throws SQLException {
        return stmt.executeLargeBatch();
    }
}
