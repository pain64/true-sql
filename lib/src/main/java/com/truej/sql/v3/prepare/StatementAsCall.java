package com.truej.sql.v3.prepare;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class StatementAsCall extends Base.Single<StatementAsCall, CallableStatement> {

    @Override CallableStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareCall(sql);
    }

    @Override StatementAsCall self() { return this; }

    @Override Void execute(CallableStatement stmt) throws SQLException {
        stmt.execute();
        return null;
    }
}
