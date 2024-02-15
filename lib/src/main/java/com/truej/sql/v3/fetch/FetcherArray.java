package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherArray extends ToPreparedStatement {
    default <T> T[] fetchArray(DataSource ds, ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchArray(cn, mapper));
    }

    default <T> T[] fetchArray(Connection cn, ResultSetMapper<T> mapper) {
        return null;
    }
}
