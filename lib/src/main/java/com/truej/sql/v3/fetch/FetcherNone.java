package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public interface FetcherNone extends ToPreparedStatement {
    default Void fetchNone(DataSource ds) {
        return TrueJdbc.withConnection(ds, this::fetchNone);
    }

    default Void fetchNone(Connection cn) {
        return null;
    }
}
