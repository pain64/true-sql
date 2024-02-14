package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherOneOrNull extends ToPreparedStatement {
    @Nullable default <T> T fetchOneOrNull(DataSource ds, Class<T> toClass) {
        return TrueJdbc.withConnection(ds, cn -> fetchOneOrNull(cn, toClass));
    }

    @Nullable default <T> T fetchOneOrNull(Connection cn, Class<T> toClass) {
        return null;
    }
}
