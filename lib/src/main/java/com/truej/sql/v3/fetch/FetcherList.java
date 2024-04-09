package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class FetcherList<T> implements
    ManagedAction.Simple<PreparedStatement, List<T>>,
    FetcherGeneratedKeys.Next<List<T>> {

    public static class Hints {
        private int expectedSize = 0;

        public Hints expectedSize(int n) {
            this.expectedSize = n;
            return this;
        }
    }

    public static <T> List<T> apply(
        RuntimeConfig conf, ResultSet rs, ResultSetMapper<T, Hints> mapper
    ) throws SQLException {
        var hints = mapper.hints();
        var iterator = mapper.map(rs);

        var result = hints != null && hints.expectedSize != 0
            ? new ArrayList<T>(hints.expectedSize)
            : new ArrayList<T>();

        while (iterator.hasNext())
            result.add(iterator.next());

        return result;
    }

    private final ResultSetMapper<T, Hints> mapper;
    public FetcherList(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willStatementBeMoved() {
        return false;
    }

    @Override public List<T> apply(
        RuntimeConfig conf, PreparedStatement stmt
    ) throws SQLException {
        return apply(conf, stmt, stmt.getResultSet());
    }

    @Override public List<T> apply(
        RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
    ) throws SQLException {
        return apply(conf, rs, this.mapper);
    }
}
