package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import com.truej.sql.v3.TrueJdbc.ResultSetMapper;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherOneOrNull extends ToPreparedStatement {
    @Nullable default <T> T fetchOneOrNull(DataSource ds, ResultSetMapper<T> mapper) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOrNull(cn, mapper));
    }

    @Nullable default <T> T fetchOneOrNull(Connection cn, ResultSetMapper<T> mapper) {
        return null;
    }
}
