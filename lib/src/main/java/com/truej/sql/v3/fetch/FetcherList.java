package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

public interface FetcherList extends ToPreparedStatement {
    default <T> List<T> fetchList(DataSource ds, Class<T> toClass) {
        return TrueJdbc.withConnection(ds, cn -> fetchList(cn, toClass));
    }

    default <T> List<T> fetchList(Connection cn, Class<T> toClass) {
        return null;
    }
}
