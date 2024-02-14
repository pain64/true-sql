package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherOne extends ToPreparedStatement {
    default <T> T fetchOne(DataSource ds, Class<T> toClass) {
        return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, toClass));
    }

    default <T> T fetchOne(Connection cn, Class<T> toClass) {
        return null;
    }
}
