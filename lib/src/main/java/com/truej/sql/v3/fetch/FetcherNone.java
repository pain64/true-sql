package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;

public class FetcherNone {
    public interface Default extends ToPreparedStatement {
        default Void fetchNone(DataSource ds) {
            return TrueJdbc.withConnection(ds, this::fetchNone);
        }

        default Void fetchNone(Connection cn) {
            // TODO: actual implementation
            return null;
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default UpdateResult<Void> fetchNone(DataSource ds) {
            return TrueJdbc.withConnection(ds, this::fetchNone);
        }

        default UpdateResult<Void> fetchNone(Connection cn) {
            var self = this;
            return WithUpdateCount.wrap(() -> ((Default) self::prepare).fetchNone(cn));
        }
    }
}
