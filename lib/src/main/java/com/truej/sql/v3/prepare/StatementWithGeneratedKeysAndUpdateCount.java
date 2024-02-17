package com.truej.sql.v3.prepare;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.fetch.*;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class StatementWithGeneratedKeysAndUpdateCount {

    // Fetchers

    public <T> UpdateResult<T> fetchOne(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, mapper));
    }

    public <T> UpdateResult<T> fetchOne(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return new UpdateResult<>(0, FetcherOne.fetch(cn, mapper));
    }

    public <T> UpdateResult<Optional<T>> fetchOneOptional(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOptional(cn, mapper));
    }

    public <T> UpdateResult<Optional<T>> fetchOneOptional(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return new UpdateResult<>(0, FetcherOneOptional.fetch(cn, mapper));
    }

    public @Nullable <T> UpdateResult<T> fetchOneOrNull(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOrNull(cn, mapper));
    }

    public @Nullable <T> UpdateResult<T> fetchOneOrNull(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return new UpdateResult<>(0, FetcherOneOrNull.fetch(cn, mapper));
    }

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

    public <T, E extends Exception> UpdateResult<T> fetch(DataSource ds, FetcherManual.PreparedStatementExecutor<T, E> executor) throws E {
        return TrueJdbc.withConnection(ds, cn -> fetch(cn, executor));
    }

    public <T, E extends Exception> UpdateResult<T> fetch(Connection cn, FetcherManual.PreparedStatementExecutor<T, E> executor) throws E {
        return new UpdateResult<>(0, FetcherManual.fetch(cn, executor));
    }
}
