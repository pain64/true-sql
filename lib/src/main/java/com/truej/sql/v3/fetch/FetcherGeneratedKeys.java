package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class FetcherGeneratedKeys<T> implements ManagedAction.Simple<PreparedStatement, T> {

    public interface Next<T> {
        boolean willPreparedStatementBeMoved();
        T apply(Concrete source) throws SQLException;
    }

    private final Next<T> next;
    public FetcherGeneratedKeys(Next<T> next) {
        this.next = next;
    }

    @Override public boolean willPreparedStatementBeMoved() {
        return next.willPreparedStatementBeMoved();
    }
    @Override public T apply(PreparedStatement stmt) throws SQLException {
        return next.apply(new Concrete(stmt, stmt.getGeneratedKeys()));
    }
}
