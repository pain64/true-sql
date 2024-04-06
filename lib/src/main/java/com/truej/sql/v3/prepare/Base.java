package com.truej.sql.v3.prepare;

import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;
import com.truej.sql.v3.source.Source;
import com.truej.sql.v3.fetch.*;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

abstract class Base<S, P extends PreparedStatement, R, U> implements With<S, P> {

    // Generated code API
    protected abstract RuntimeException mapException(SQLException e);
    protected abstract String query();
    protected abstract void bindArgs(P stmt) throws SQLException;
    // End

    public interface StatementConfigurator<S extends PreparedStatement> {
        void configure(S statement) throws SQLException;
    }

    StatementConfigurator<P> afterPrepare = __ -> { };
    @Nullable StatementConstructor<P> constructor = null;

    abstract S self();
    abstract P defaultConstructor(Connection connection, String sql) throws SQLException;
    abstract R execute(P stmt) throws SQLException;
    abstract U getUpdateCount(R executionResult, P stmt) throws SQLException;

    public S afterPrepare(StatementConfigurator<P> config) {
        this.afterPrepare = config;
        return self();
    }

    @Override public S with(StatementConstructor<P> constructor) {
        this.constructor = constructor;
        return self();
    }

    private <T> T managed(Connection connection, ManagedAction.Full<P, R, T> action) {
        try {
            var triedToClose = false;
            var stmt = constructor != null
                ? constructor.construct(connection, query())
                : defaultConstructor(connection, query());

            try {
                bindArgs(stmt);
                afterPrepare.configure(stmt);
                var execResult = execute(stmt);

                return action.apply(execResult, stmt);
            } catch (Exception e) {
                triedToClose = true;
                try {
                    stmt.close();
                } catch (Exception e2) {
                    e.addSuppressed(e2);
                }

                throw e;
            } finally {
                if (!action.willPreparedStatementBeMoved() && !triedToClose)
                    stmt.close();
            }
        } catch (SQLException e) {
            throw mapException(e);
        }
    }

    private <T> T managed(Source source, ManagedAction.Full<P, R, T> action) {
        return switch (source) {
            case ConnectionW cn -> managed(cn.w(), action);
            case DataSourceW ds -> ds.withConnection(cn -> managed(cn.w(), action));
        };
    }

    private <T> T managedS(Source source, ManagedAction.Simple<PreparedStatement, T> action) {
        // Java has no declaration-site variance support (.)(.)
        // noinspection unchecked
        return managed(source, (ManagedAction.Full<P, R, T>) action);
    }

    public Void fetchNone(Source source) {
        return managedS(source, new FetcherNone());
    }

    public <T> T fetchOne(Source source, ResultSetMapper<T, FetcherOne.Hints> mapper) {
        return managedS(source, new FetcherOne<>(mapper));
    }

    public <T> Optional<T> fetchOneOptional(
        Source source, ResultSetMapper<T, FetcherOneOptional.Hints> mapper
    ) {
        return managedS(source, new FetcherOneOptional<>(mapper));
    }

    public <T> @Nullable T fetchOneOrNull(
        Source source, ResultSetMapper<T, FetcherOneOrNull.Hints> mapper
    ) {
        return managedS(source, new FetcherOneOrNull<>(mapper));
    }

    public <T> List<T> fetchList(Source source, ResultSetMapper<T, FetcherList.Hints> mapper) {
        return managedS(source, new FetcherList<>(mapper));
    }

    public <T> Stream<T> fetchStream(
        Source source, ResultSetMapper<T, FetcherStream.Hints> mapper
    ) {
        return managedS(source, new FetcherStream<>(mapper));
    }

    public <T> T fetchGeneratedKeys(Source source, FetcherGeneratedKeys.Next<T> next) {
        return managedS(source, new FetcherGeneratedKeys<>(next));
    }

    public <T> T fetchManual(Source source, ManagedAction.Full<P, R, T> next) {
        return managed(source, next);
    }

    public U fetchUpdateCount(Source source) {
        return fetchUpdateCount(source, new ManagedAction.Simple<>() {
            @Override public R apply(P stmt) { return null; }
            @Override public boolean willPreparedStatementBeMoved() { return false; }
        }).updateCount;
    }

    public <T> UpdateResult<U, T> fetchUpdateCount(
        Source source, ManagedAction.Simple<P, T> next
    ) {
        return managed(source, new ManagedAction.Full<>() {
            @Override public boolean willPreparedStatementBeMoved() {
                return next.willPreparedStatementBeMoved();
            }
            @Override
            public UpdateResult<U, T> apply(R executionResult, P stmt) throws SQLException {
                return new UpdateResult<>(
                    getUpdateCount(executionResult, stmt),
                    next.apply(stmt)
                );
            }
        });
    }

    abstract static class Batch<S, P extends PreparedStatement> extends Base<S, P, long[], long[]> {
        @Override long[] getUpdateCount(long[] executionResult, P stmt) {
            return executionResult;
        }
    }

    abstract static class Single<S, P extends PreparedStatement> extends Base<S, P, Void, Long> {
        @Override Long getUpdateCount(Void executionResult, P stmt) throws SQLException {
            return stmt.getLargeUpdateCount();
        }
    }
}
