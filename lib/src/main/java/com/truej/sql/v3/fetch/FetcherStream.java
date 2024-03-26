package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;
import com.truej.sql.v3.SqlExceptionR;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Iterator;

public class FetcherStream<T> implements
    FetcherUpdateCount.Next<Stream<T>>,
    FetcherGeneratedKeys.Next<Stream<T>> {

    private final ResultSetMapper<T, Hints> mapper;

    @Override public boolean willPreparedStatementBeMoved() {
        return true;
    }
    @Override public Stream<T> apply(PreparedStatement stmt) throws SQLException {
        return apply(new Concrete(stmt, stmt.getResultSet()));
    }
    @Override public Stream<T> apply(Concrete source) throws SQLException {
        final Iterator<T> iterator;
        // FIXME: remove this try/catch???
        try {
            iterator = mapper.map(source.rs);
        } catch (Exception e) {
            try {
                source.rs.getStatement().close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }

            throw e;
        }

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false
        ).onClose(() -> {
            try {
                // FIXME
                source.rs.getStatement().close();
            } catch (SQLException e) {
                // FIXME: mapException???
                throw new SqlExceptionR(e);
            }
        });
    }

    public FetcherStream(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    public static class Hints { }

    public interface Instance extends ToPreparedStatement {
        default <T> Stream<T> fetchStream(
            Source source, ResultSetMapper<T, Hints> mapper
        ) {
            return managed(
                source, () -> true, stmt -> new FetcherStream<>(mapper).apply(stmt)
            );
        }
    }
}
