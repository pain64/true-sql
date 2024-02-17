package com.truej.sql.v3.prepare;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.fetch.FetcherArray;
import com.truej.sql.v3.fetch.FetcherList;
import com.truej.sql.v3.fetch.FetcherManual;
import com.truej.sql.v3.fetch.FetcherStream;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Stream;

public class BatchStatementWithGeneratedKeysAndUpdateCount {
    // Fetchers

    public <T> UpdateResult<T[]> fetchArray(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchArray(cn, mapper));
    }

    public <T> UpdateResult<T[]> fetchArray(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return new UpdateResult<>(0, FetcherArray.fetch(cn, mapper));
    }

    public <T> UpdateResult<List<T>> fetchList(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchList(cn, mapper));
    }

    public <T> UpdateResult<List<T>> fetchList(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return new UpdateResult<>(0, FetcherList.fetch(cn, mapper));
    }

    public  <T> UpdateResult<Stream<T>> fetchStream(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchStream(cn, mapper));
    }

    public <T> UpdateResult<Stream<T>> fetchStream(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return new UpdateResult<>(0, FetcherStream.fetch(cn, mapper));
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
