package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.stream.Stream;

public interface FetcherStream extends ToPreparedStatement {
    default <T> Stream<T> fetchStream(DataSource ds, ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchStream(cn, mapper));
    }

    default <T> Stream<T> fetchStream(Connection cn, ResultSetMapper<T> mapper) {
        return null;
    }
}
