package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public class FetcherOneOptional<T> implements
    ToPreparedStatement.ManagedAction<Optional<T>>,
    FetcherUpdateCount.Next<Optional<T>>,
    FetcherGeneratedKeys.Next<Optional<T>> {

    public static class Hints { }

    public static <T> Optional<T> apply(
        ResultSet rs, ResultSetMapper<T, Hints> mapper
    ) throws SQLException {
        return Optional.ofNullable(
            FetcherOneOrNull.apply(rs, new ResultSetMapper<>() {
                @Override public @Nullable FetcherOneOrNull.Hints hints() { return null; }
                @Override public Iterator<T> map(ResultSet rs) { return mapper.map(rs); }
            })
        );
    }

    private final ResultSetMapper<T, Hints> mapper;
    public FetcherOneOptional(ResultSetMapper<T, Hints> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willPreparedStatementBeMoved() {
        return false;
    }
    @Override public Optional<T> apply(PreparedStatement stmt) throws SQLException {
        return apply(new Concrete(stmt, stmt.getResultSet()));
    }
    @Override public Optional<T> apply(Concrete source) throws SQLException {
        return apply(source.rs, this.mapper);
    }

    public interface Instance extends ToPreparedStatement {
        default <T> Optional<T> fetchOneOptional(
            Source source, ResultSetMapper<T, Hints> mapper
        ) {
            return managed(source, new FetcherOneOptional<>(mapper));
        }
    }
}
