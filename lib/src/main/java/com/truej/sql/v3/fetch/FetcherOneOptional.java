package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public final class FetcherOneOptional<T> implements
    ManagedAction.Simple<PreparedStatement, Optional<T>>,
    FetcherGeneratedKeys.Next<Optional<T>> {

    public static <T> Optional<T> apply(
        RuntimeConfig conf, ResultSet rs, ResultSetMapper<T, Void> mapper
    ) throws SQLException {
        return Optional.ofNullable(
            FetcherOneOrNull.apply(rs, new ResultSetMapper<>() {
                @Override public @Nullable Void hints() { return null; }
                @Override public Iterator<T> map(ResultSet rs) { return mapper.map(rs); }
            })
        );
    }

    private final ResultSetMapper<T, Void> mapper;
    public FetcherOneOptional(ResultSetMapper<T, Void> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willStatementBeMoved() {
        return false;
    }

    @Override public Optional<T> apply(
        RuntimeConfig conf, PreparedStatement stmt
    ) throws SQLException {
        return apply(conf, stmt, stmt.getResultSet());
    }

    @Override public Optional<T> apply(
        RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
    ) throws SQLException {
        return apply(conf, rs, this.mapper);
    }
}
