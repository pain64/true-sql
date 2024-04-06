package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.prepare.ManagedAction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Iterator;

public final class FetcherStream<T> implements
    ManagedAction.Simple<PreparedStatement, Stream<T>>, FetcherGeneratedKeys.Next<Stream<T>> {

    private final ResultSetMapper<T, Hints> mapper;

    @Override public boolean willPreparedStatementBeMoved() {
        return true;
    }
    @Override public Stream<T> apply(PreparedStatement stmt) throws SQLException {
        return apply(new Concrete(stmt, stmt.getResultSet()));
    }
    @Override public Stream<T> apply(Concrete source) throws SQLException {
        final Iterator<T> iterator;
        iterator = mapper.map(source.rs());

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false
        ).onClose(() -> {
            try {
                source.stmt().close();
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        });
    }

    public FetcherStream(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    public static class Hints { }
}
