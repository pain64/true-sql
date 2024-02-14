package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;

public interface FetcherOneOptional extends ToPreparedStatement {
    default <T> Optional<T> fetchOneOptional(DataSource ds, Class<T> toClass) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOptional(cn, toClass));
    }

    default <T> Optional<T> fetchOneOptional(Connection cn, Class<T> toClass) {
        return null;
    }
}
