package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class FetcherGeneratedKeys<T> implements ManagedAction.Simple<PreparedStatement, T> {

    public interface Next<T> {
        boolean willStatementBeMoved();
        T apply(RuntimeConfig conf, PreparedStatement stmt, ResultSet rs) throws SQLException;
    }

    private final Next<T> next;
    public FetcherGeneratedKeys(Next<T> next) {
        this.next = next;
    }

    @Override public boolean willStatementBeMoved() {
        return next.willStatementBeMoved();
    }

    @Override public T apply(RuntimeConfig conf, PreparedStatement stmt) throws SQLException {
        return next.apply(conf, stmt, stmt.getGeneratedKeys());
    }
}
