package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class FetcherNone implements
    ManagedAction.Simple<PreparedStatement, Void>,
    FetcherGeneratedKeys.Next<Void> {

    @Override public boolean willStatementBeMoved() { return false; }

    @Override public Void apply(RuntimeConfig conf, PreparedStatement stmt) throws SQLException {
        return apply(conf, stmt, stmt.getResultSet());
    }

    @Override public Void apply(
        RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
    ) throws SQLException {
        return null;
    }

}
