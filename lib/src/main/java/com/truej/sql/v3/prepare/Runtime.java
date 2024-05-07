package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;
import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;
import com.truej.sql.v3.source.RuntimeConfig;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Runtime {
    public static ResultSet getResultSet(
        PreparedStatement stmt, boolean hasGeneratedKeys
    ) throws SQLException {
        return hasGeneratedKeys ? stmt.getGeneratedKeys() : stmt.getResultSet();
    }

    private static <S, P extends PreparedStatement, R, U, T>
    T managed(Base<S, P, R, U> base, ConnectionW connection, ManagedAction<P, R, T> action) {
        try {
            var triedToClose = false;

            var stmt = base.constructor != null
                ? base.constructor.construct(connection.w(), base.query())
                : base.defaultConstructor(connection.w(), base.query());

            var hasGeneratedKeys = base.constructor != null && base.constructor.hasGeneratedKeys();

            try {
                base.bindArgs(stmt);
                base.afterPrepare.configure(stmt);
                var execResult = base.execute(stmt);

                return action.apply(connection, execResult, stmt, hasGeneratedKeys);
            } catch (Exception e) {
                triedToClose = true;
                try {
                    stmt.close();
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                }

                throw e;
            } finally {
                if (!action.willStatementBeMoved() && !triedToClose)
                    stmt.close();
            }
        } catch (SQLException e) {
            throw connection.mapException(e);
        }
    }

    private static <S, P extends PreparedStatement, R, U, T>
    T managed(Base<S, P, R, U> base, ManagedAction<P, R, T> action) {
        return switch (base.source()) {
            case ConnectionW cn -> managed(base, cn, action);
            case DataSourceW ds -> ds.withConnection(cn -> managed(base, cn, action));
        };
    }

    private static <S, P extends PreparedStatement, R, U, T>
    T managedS(Base<S, P, R, U> base, ManagedAction<PreparedStatement, R, T> action) {
        // Java has no declaration-site variance support (.)(.)
        // noinspection unchecked
        return managed(base, (ManagedAction<P, R, T>) action);
    }

    public static <S, P extends PreparedStatement, R, U>
    Void fetchNone(Base<S, P, R, U> base) {
        return managedS(base, new FetcherNone<>());
    }

    public static <S, P extends PreparedStatement, R, U, T>
    T fetchOne(Base<S, P, R, U> base, ResultSetMapper<T> mapper) {
        return managedS(base, new FetcherOne<>(mapper));
    }

    public static <S, P extends PreparedStatement, R, U, T>
    Optional<T> fetchOneOptional(Base<S, P, R, U> base, ResultSetMapper<T> mapper) {
        return managedS(base, new FetcherOneOptional<>(mapper));
    }

    public static <S, P extends PreparedStatement, R, U, T>
    @Nullable T fetchOneOrNull(Base<S, P, R, U> base, ResultSetMapper<T> mapper) {
        return managedS(base, new FetcherOneOrNull<>(mapper));
    }

    public static <S, P extends PreparedStatement, R, U, T>
    List<T> fetchList(Base<S, P, R, U> base, ResultSetMapper<T> mapper) {
        return managedS(base, new FetcherList<>(mapper));
    }

    public static <S, P extends PreparedStatement, R, U, T>
    List<T> fetchList(Base<S, P, R, U> base, ResultSetMapper<T> mapper, int expectedSize) {
        return managedS(base, new FetcherList<>(mapper, expectedSize));
    }

    public static <S, P extends PreparedStatement, R, U, T>
    Stream<T> fetchStream(Base<S, P, R, U> base, ResultSetMapper<T> mapper) {
        return managedS(base, new FetcherStream<>(mapper));
    }

    public static <S, P extends PreparedStatement, R, U, T> T
    fetch(Base<S, P, R, U> base, ManagedAction<P, R, T> next) {
        return managed(base, next);
    }

    public <S, P extends PreparedStatement, R, U>
    U fetchUpdateCount(Base<S, P, R, U> base) {
        return fetchUpdateCount(base, new ManagedAction<>() {
            @Override public boolean willStatementBeMoved() { return false; }
            @Override public Object apply(
                RuntimeConfig conf, R executionResult, P stmt, boolean hasGeneratedKeys
            ) {
                return null;
            }
        }).updateCount;
    }

    public <S, P extends PreparedStatement, R, U, T>
    UpdateResult<U, T> fetchUpdateCount(Base<S, P, R, U> base, ManagedAction<P, R, T> next) {
        return managed(base, new ManagedAction<>() {
            @Override public boolean willStatementBeMoved() {
                return next.willStatementBeMoved();
            }
            @Override
            public UpdateResult<U, T> apply(
                RuntimeConfig conf, R executionResult, P stmt, boolean hasGeneratedKeys
            ) throws SQLException {
                return new UpdateResult<>(
                    base.getUpdateCount(executionResult, stmt),
                    next.apply(conf, executionResult, stmt, hasGeneratedKeys)
                );
            }
        });
    }
}
