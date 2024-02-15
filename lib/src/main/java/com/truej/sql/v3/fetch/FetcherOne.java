package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherOne extends ToPreparedStatement {
    default <T> T fetchOne(DataSource ds, ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOne(cn, mapper));
    }

    default <T> T fetchOne(Connection cn, ResultSetMapper<T> mapper) {
        return null;
    }
}
