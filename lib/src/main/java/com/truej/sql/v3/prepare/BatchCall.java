package com.truej.sql.v3.prepare;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.fetch.*;
import com.truej.sql.v3.fetch.FetcherManual.PreparedStatementExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class BatchCall {

    public <T> UpdateResult<T> withUpdateCount(
        Function<BatchCallWithUpdateCount, T> stmt
    ) {
        return withUpdateCount(false, stmt);
    }

    public <T> UpdateResult<T> withUpdateCount(
        boolean isLarge, Function<BatchCallWithUpdateCount, T> stmt
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

    public <T> T[] fetchArray(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchArray(cn, mapper));
    }

    public <T> T[] fetchArray(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return FetcherArray.fetch(cn, mapper);
    }

    public <T> List<T> fetchList(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchList(cn, mapper));
    }

    public <T> List<T> fetchList(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return FetcherList.fetch(cn, mapper);
    }

    public <T> Stream<T> fetchStream(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchStream(cn, mapper));
    }

    public <T> Stream<T> fetchStream(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return FetcherStream.fetch(cn, mapper);
    }

    public <T, E extends Exception> T fetch(DataSource ds, PreparedStatementExecutor<T, E> executor) throws E {
        return TrueJdbc.withConnection(ds, cn -> fetch(cn, executor));
    }

    public <T, E extends Exception> T fetch(Connection cn, PreparedStatementExecutor<T, E> executor) throws E {
        return FetcherManual.fetch(cn, executor);
    }
}
