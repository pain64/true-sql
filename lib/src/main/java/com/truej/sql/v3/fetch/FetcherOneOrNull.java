package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class FetcherOneOrNull<T> implements
    ManagedAction.Simple<PreparedStatement, @Nullable T>,
    FetcherGeneratedKeys.Next<@Nullable T> {

    public static <T> @Nullable T apply(
        ResultSet rs, ResultSetMapper<T, Void> mapper
    ) throws SQLException {
        var iterator = mapper.map(rs);

        if (iterator.hasNext()) {
            var result = iterator.next();
            if (iterator.hasNext())
                throw new TooMuchRowsException();
            return result;
        }

        return null;
    }

    private final ResultSetMapper<T, Void> mapper;
    public FetcherOneOrNull(ResultSetMapper<T, Void> mapper) {
        this.mapper = mapper;
    }

    @Override public boolean willStatementBeMoved() {
        return false;
    }

    @Override public @Nullable T apply(
        RuntimeConfig conf, PreparedStatement stmt
    ) throws SQLException {
        return apply(conf, stmt, stmt.getResultSet());
    }

    @Override public @Nullable T apply(
        RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
    ) throws SQLException {
        return apply(conf, rs, mapper);
    }

    public static <T> @Nullable T apply(
        RuntimeConfig conf, ResultSet rs, ResultSetMapper<T, Void> mapper
    ) throws SQLException {
        return apply(rs, mapper);
    }
}
