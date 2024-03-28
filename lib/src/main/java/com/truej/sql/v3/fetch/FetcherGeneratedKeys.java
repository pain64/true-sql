package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherGeneratedKeys<R, T> implements
    ToPreparedStatement.ManagedAction<R, T>,
    FetcherUpdateCount.Next<T> {

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

    public interface Instance extends ToPreparedStatement {
        default <T> T fetchGeneratedKeys(Source source, Next<T> next) {
            return managed(source, new FetcherGeneratedKeys<>(next));
        }
    }
}
