package com.truej.sql.v3.prepare;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.fetch.FetcherManual;
import com.truej.sql.v3.fetch.FetcherNone;
import com.truej.sql.v3.fetch.FetcherOne;

import javax.sql.DataSource;
import java.sql.Connection;

public class CallWithUpdateCount {
    // Fetchers

    public UpdateResult<Void> fetchNone(DataSource ds) {
        return TrueJdbc.withConnection(ds, this::fetchNone);
    }

    public UpdateResult<Void> fetchNone(Connection cn) {
        return new UpdateResult<>(0, FetcherNone.fetch(cn));
    }

    public <T> UpdateResult<T> fetchOne(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, mapper));
    }

    public <T> UpdateResult<T> fetchOne(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return new UpdateResult<>(0, FetcherOne.fetch(cn, mapper));
    }

    public <T, E extends Exception> UpdateResult<T> fetch(
        DataSource ds, FetcherManual.PreparedStatementExecutor<T, E> executor
    ) throws E {
        return TrueJdbc.withConnection(ds, cn -> fetch(cn, executor));
    }

    public <T, E extends Exception> UpdateResult<T> fetch(
        Connection cn, FetcherManual.PreparedStatementExecutor<T, E> executor
    ) throws E {
        return new UpdateResult<>(0, FetcherManual.fetch(cn, executor));
    }
}
