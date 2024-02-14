package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherNone extends ToPreparedStatement {
    default <T> T fetchNone(DataSource ds) {
        return TrueJdbc.withConnection(ds, cn -> fetchNone(cn));
    }

    default <T> T fetchNone(Connection cn) {
        return null;
    }
}
