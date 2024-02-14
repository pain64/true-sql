package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.stream.Stream;

public interface FetcherStream extends ToPreparedStatement {
    default <T> Stream<T> fetchStream(DataSource ds, Class<T> toClass) {
        return TrueJdbc.withConnection(ds, cn -> fetchStream(cn, toClass));
    }

    default <T> Stream<T> fetchStream(Connection cn, Class<T> toClass) {
        return null;
    }
}
