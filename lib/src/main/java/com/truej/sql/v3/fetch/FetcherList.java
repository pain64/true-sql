package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.TrueJdbc.ResultSetMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public interface FetcherList extends ToPreparedStatement {
    default <T> List<T> fetchList(DataSource ds, ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchList(cn, mapper));
    }

    default <T> List<T> fetchList(Connection cn, ResultSetMapper<T> mapper) {
        return null;
    }
}
