package com.truej.sql.v3.prepare;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.fetch.*;
import com.truej.sql.v3.fetch.FetcherManual.PreparedStatementExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Function;

public final class Call {

    public <T> UpdateResult<T> withUpdateCount(
        Function<CallWithUpdateCount, T> stmt
    ) {
        return withUpdateCount(false, stmt);
    }

    public <T> UpdateResult<T> withUpdateCount(
        boolean isLarge, Function<CallWithUpdateCount, T> stmt
    ) {
        return null;
    }

    // Fetchers

    public Void fetchNone(DataSource ds) {
        return TrueJdbc.withConnection(ds, this::fetchNone);
    }

    public Void fetchNone(Connection cn) {
        return FetcherNone.fetch(cn);
    }

    public <T> T fetchOne(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, mapper));
    }

    public <T> T fetchOne(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return FetcherOne.fetch(cn, mapper);
    }

    public <T, E extends Exception> T fetch(DataSource ds, PreparedStatementExecutor<T, E> executor) throws E {
        return TrueJdbc.withConnection(ds, cn -> fetch(cn, executor));
    }

    public <T, E extends Exception> T fetch(Connection cn, PreparedStatementExecutor<T, E> executor) throws E {
        return FetcherManual.fetch(cn, executor);
    }
}
