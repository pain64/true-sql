package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class FetcherOneOptional<T, R> implements
    ManagedAction<PreparedStatement, R, Optional<T>> {

    public static <T> Optional<T> apply(
        RuntimeConfig conf, ResultSet rs, ResultSetMapper<T, Void> mapper
    ) throws SQLException {
        return Optional.ofNullable(FetcherOneOrNull.apply(rs, mapper));
    }

    private final ResultSetMapper<T, Void> mapper;
    public FetcherOneOptional(ResultSetMapper<T, Void> mapper) { this.mapper = mapper; }

    @Override public boolean willStatementBeMoved() { return false; }

    @Override public Optional<T> apply(
        RuntimeConfig conf, R executionResult, PreparedStatement stmt, boolean hasGeneratedKeys
    ) throws SQLException {
        return apply(conf, SomeLogic.getResultSet(stmt, hasGeneratedKeys), this.mapper);
    }
}
