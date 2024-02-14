package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherArray extends ToPreparedStatement {
    default <T> T[] fetchArray(DataSource ds, Class<T> toClass) {
        return TrueJdbc.withConnection(ds, cn -> fetchArray(cn, toClass));
    }

    default <T> T[] fetchArray(Connection cn, Class<T> toClass) {
        return null;
    }
}
