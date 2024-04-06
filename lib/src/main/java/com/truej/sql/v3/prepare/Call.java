package com.truej.sql.v3.prepare;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class Call extends Base.Single<Call, CallableStatement> {

    @Override CallableStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareCall(sql);
    }

    @Override Call self() { return this; }

    @Override Void execute(CallableStatement stmt) throws SQLException {
        stmt.execute();
        return null;
    }
}