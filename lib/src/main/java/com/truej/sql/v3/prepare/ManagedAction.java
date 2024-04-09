package com.truej.sql.v3.prepare;

import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ManagedAction {

    interface Full<P extends PreparedStatement, R, T> {
        boolean willStatementBeMoved();
        T apply(RuntimeConfig conf, R executionResult, P stmt) throws SQLException;
    }

    interface Simple<P extends PreparedStatement, T> extends Full<P, Object, T> {
        @Override default T apply(
            RuntimeConfig conf, Object executionResult, P stmt
        ) throws SQLException {
            return apply(conf, stmt);
        }

        T apply(RuntimeConfig conf, P stmt) throws SQLException;
    }
}
