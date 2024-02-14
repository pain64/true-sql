package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherManual extends ToPreparedStatement {
    class ExecutionContext {
    }

    interface PreparedStatementExecutor<T, E extends Exception> {
        T execute(ExecutionContext ctx) throws E;
    }

    default <T, E extends Exception> T fetch(DataSource ds, PreparedStatementExecutor<T, E> executor) throws E {
        return TrueJdbc.withConnection(ds, cn -> fetch(cn, executor));
    }

    default <T, E extends Exception> T fetch(Connection cn, PreparedStatementExecutor<T, E> executor) throws E {
        return null;
    }
}
