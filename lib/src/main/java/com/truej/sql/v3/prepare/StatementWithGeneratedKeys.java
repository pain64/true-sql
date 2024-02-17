package com.truej.sql.v3.prepare;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.fetch.*;
import com.truej.sql.v3.fetch.FetcherManual.PreparedStatementExecutor;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class StatementWithGeneratedKeys {

    public StatementWithGeneratedKeysAndUpdateCount withUpdateCount() {
        return withUpdateCount(false);
    }

    public StatementWithGeneratedKeysAndUpdateCount withUpdateCount(boolean isLarge) {
        return null;
    }

    // Fetchers

    public <T> T fetchOne(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, mapper));
    }

    public <T> T fetchOne(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return FetcherOne.fetch(cn, mapper);
    }

    public <T> Optional<T> fetchOneOptional(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOptional(cn, mapper));
    }

    public <T> Optional<T> fetchOneOptional(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return FetcherOneOptional.fetch(cn, mapper);
    }

    public @Nullable <T> T fetchOneOrNull(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOrNull(cn, mapper));
    }

    public @Nullable <T> T fetchOneOrNull(Connection cn, TrueJdbc.ResultSetMapper<T> mapper) {
        return FetcherOneOrNull.fetch(cn, mapper);
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

    public  <T> Stream<T> fetchStream(DataSource ds, TrueJdbc.ResultSetMapper<T> mapper) {
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
