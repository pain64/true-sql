package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Iterator;

public final class FetcherStream<T> implements
    ManagedAction.Simple<PreparedStatement, Stream<T>>,
    FetcherGeneratedKeys.Next<Stream<T>> {

    private final ResultSetMapper<T, Void> mapper;
    public FetcherStream(ResultSetMapper<T, Void> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willStatementBeMoved() {
        return true;
    }

    public static <T> Stream<T> apply(
        RuntimeConfig conf, PreparedStatement stmt,
        ResultSet rs, ResultSetMapper<T, Void> mapper
    ) throws SQLException {
        final Iterator<T> iterator;
        iterator = mapper.map(rs);

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false
        ).onClose(() -> {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw conf.mapException(e);
            }
        });
    }

    @Override public Stream<T> apply(
        RuntimeConfig conf, PreparedStatement stmt
    ) throws SQLException {
        return apply(conf, stmt, stmt.getResultSet());
    }

    @Override public Stream<T> apply(
        RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
    ) throws SQLException {
        return apply(conf, stmt, rs, mapper);
    }
}
