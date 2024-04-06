package com.truej.sql.v3.prepare;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ManagedAction {

    interface Full<P extends PreparedStatement, R, T> {
        boolean willPreparedStatementBeMoved();
        T apply(R executionResult, P stmt) throws SQLException;
    }

    interface Simple<P extends PreparedStatement, T> extends Full<P, Object, T> {
        @Override default T apply(Object executionResult, P stmt) throws SQLException {
            return apply(stmt);
        }
        T apply(P stmt) throws SQLException;
    }
}
