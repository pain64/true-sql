package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class FetcherList<T, R> implements
    ManagedAction<PreparedStatement, R, List<T>> {

//    public static class Hints {
//        private int expectedSize = 0;
//
//        public Hints expectedSize(int n) {
//            this.expectedSize = n;
//            return this;
//        }
//    }

    private final ResultSetMapper<T> mapper;
    private final int expectedSize;

    public FetcherList(ResultSetMapper<T> mapper) {
        this.mapper = mapper;
        this.expectedSize = 0;
    }

    public FetcherList(ResultSetMapper<T> mapper, int expectedSize) {
        this.mapper = mapper;
        this.expectedSize = expectedSize;
    }

    @Override public boolean willStatementBeMoved() { return false; }

    @Override public List<T> apply(
        RuntimeConfig conf, R executionResult, PreparedStatement stmt, boolean hasGeneratedKeys
    ) throws SQLException {
        var iterator = mapper.map(
            Runtime.getResultSet(stmt, hasGeneratedKeys)
        );

        var result = expectedSize != 0
            ? new ArrayList<T>(expectedSize)
            : new ArrayList<T>();

        while (iterator.hasNext())
            result.add(iterator.next());

        return result;
    }
}
