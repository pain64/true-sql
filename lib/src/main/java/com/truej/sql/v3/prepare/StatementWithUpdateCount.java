package com.truej.sql.v3.prepare;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.fetch.*;
import com.truej.sql.v3.fetch.FetcherManual.PreparedStatementExecutor;

import javax.sql.DataSource;
import java.sql.Connection;

public class StatementWithUpdateCount {

    // Fetchers

    public UpdateResult<Void> fetchNone(DataSource ds) {
        return TrueJdbc.withConnection(ds, this::fetchNone);
    }

    public UpdateResult<Void> fetchNone(Connection cn) {
        return new UpdateResult<>(0, FetcherNone.fetch(cn));
    }

    public <T, E extends Exception> UpdateResult<T> fetch(
        DataSource ds, PreparedStatementExecutor<T, E> executor

    ) throws E {
        return TrueJdbc.withConnection(ds, cn -> fetch(cn, executor));
    }

    public <T, E extends Exception> UpdateResult<T> fetch(
        Connection cn, PreparedStatementExecutor<T, E> executor
    ) throws E {
        return new UpdateResult<>(0, FetcherManual.fetch(cn, executor));
    }
}
