package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class FetcherNone implements
    ManagedAction.Simple<PreparedStatement, Void>, FetcherGeneratedKeys.Next<Void> {

    public static Void apply(ResultSet rs) {
        return null;
    }

    @Override public Void apply(PreparedStatement stmt) throws SQLException {
        return apply(stmt.getResultSet());
    }
    @Override public Void apply(Concrete source) throws SQLException {
        return apply(source.rs());
    }
    @Override public boolean willPreparedStatementBeMoved() { return false; }
}
