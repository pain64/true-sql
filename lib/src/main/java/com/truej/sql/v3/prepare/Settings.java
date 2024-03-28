package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.ToPreparedStatement;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

abstract class Settings<T, U, S extends PreparedStatement>
    implements ToPreparedStatement {

    public abstract String query();
    public abstract void bindArgs(S stmt) throws SQLException;

    public interface StatementConfigurator<S extends PreparedStatement> {
        void configure(S statement) throws SQLException;
    }

    public interface StatementConstructor<S extends PreparedStatement> {
        S construct(Connection connection, String sql) throws SQLException;
    }

    protected StatementConfigurator<S> afterPrepare = __ -> { };
    protected @Nullable StatementConstructor<S> constructor = null;

    abstract T self();
    abstract S defaultConstructor(Connection connection, String sql) throws SQLException;
    abstract void execute(S stmt) throws SQLException;
    // abstract protected U getUpdateCount(PreparedStatement stmt) throws SQLException;

    public T afterPrepare(StatementConfigurator<S> config) {
        this.afterPrepare = config;
        return self();
    }

    public T with(StatementConstructor<S> constructor) {
        this.constructor = constructor;
        return self();
    }

    public S prepareAndExecute(Connection cn) throws SQLException {
        var stmt = constructor != null
            ? constructor.construct(cn, query())
            : defaultConstructor(cn, query());

        try {
            bindArgs(stmt);
            afterPrepare.configure(stmt);
            execute(stmt);
        } catch (Exception e) {
            try {
                stmt.close();
            } catch (Exception closeError) {
                e.addSuppressed(closeError);
            }

            throw e;
        }

        return stmt;
    }
}
