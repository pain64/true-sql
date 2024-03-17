package com.truej.sql.v3.prepare;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class CallSettings<T> extends Settings <T, CallableStatement> {
    @Override CallableStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareCall(query());
    }
}
