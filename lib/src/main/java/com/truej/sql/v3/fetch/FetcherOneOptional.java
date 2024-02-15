package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;

public interface FetcherOneOptional extends ToPreparedStatement {
    default <T> Optional<T> fetchOneOptional(DataSource ds, ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOptional(cn, mapper));
    }

    default <T> Optional<T> fetchOneOptional(Connection cn, ResultSetMapper<T> mapper) {
        return null;
    }
}
