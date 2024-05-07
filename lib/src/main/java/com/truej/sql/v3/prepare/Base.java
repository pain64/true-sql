package com.truej.sql.v3.prepare;

import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;
import com.truej.sql.v3.source.RuntimeConfig;
import com.truej.sql.v3.source.Source;
import com.truej.sql.v3.fetch.*;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// BatchPrepared
//          .afterPrepare +
//          .asGeneratedKeys -> BatchPrepared -> fetch(xxx)
// Prepared
//          .afterPrepare +
//          .asGeneratedKeys -> BatchPrepared -> fetch(xxx)
public abstract class Base<S, P extends PreparedStatement, R, U> implements With<S, P>, FetchApi<P, R, U> {

    // Generated code API
    protected abstract Source source();
    protected abstract String query();
    protected abstract void bindArgs(P stmt) throws SQLException;
    // End

    public interface StatementConfigurator<P extends PreparedStatement> {
        void configure(P statement) throws SQLException;
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

    public final FetchApi<P, R, U> g = this;

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