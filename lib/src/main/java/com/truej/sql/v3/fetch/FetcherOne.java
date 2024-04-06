package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class FetcherOne<T> implements
    ManagedAction.Simple<PreparedStatement, T>, FetcherGeneratedKeys.Next<T> {

    public static class Hints { }

    public static <T> T apply(
        ResultSet rs, ResultSetMapper<T, Hints> mapper
    ) throws SQLException {
        var iterator = mapper.map(rs);

        if (iterator.hasNext()) {
            var result = iterator.next();
            if (iterator.hasNext())
                throw new TooMuchRowsException();
            return result;
        }

        throw new TooFewRowsException();
    }

    private final ResultSetMapper<T, Hints> mapper;
    public FetcherOne(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willPreparedStatementBeMoved() {
        return false;
    }
    @Override public T apply(PreparedStatement stmt) throws SQLException {
        return apply(new Concrete(stmt, stmt.getResultSet()));
    }
    @Override public T apply(Concrete source) throws SQLException {
        return apply(source.rs(), this.mapper);
    }
}
