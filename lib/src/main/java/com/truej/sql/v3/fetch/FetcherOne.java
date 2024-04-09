package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class FetcherOne<T> implements
    ManagedAction.Simple<PreparedStatement, T>,
    FetcherGeneratedKeys.Next<T> {

    public static <T> T apply(
        RuntimeConfig conf, ResultSet rs, ResultSetMapper<T, Void> mapper
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

    private final ResultSetMapper<T, Void> mapper;
    public FetcherOne(ResultSetMapper<T, Void> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willStatementBeMoved() {
        return false;
    }

    @Override public T apply(
        RuntimeConfig conf, PreparedStatement stmt
    ) throws SQLException {
        return apply(conf, stmt, stmt.getResultSet());
    }

    @Override public T apply(
        RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
    ) throws SQLException {
        return apply(conf, rs, this.mapper);
    }
}
