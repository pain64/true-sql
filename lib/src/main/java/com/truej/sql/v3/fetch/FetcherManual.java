package com.truej.sql.v3.fetch;

import com.truej.sql.v3.TrueJdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FetcherManual {

    // FIXME: PreparedCall
    public interface PreparedStatementFetcher<T, E extends Exception> {
        T fetch(PreparedStatement stmt) throws E;
    }

    public static <T, E extends Exception> T fetch(
        Connection cn, PreparedStatementFetcher<T, E> fetcher
    ) throws E {
        return null;
    }

    public interface Default extends ToPreparedStatement {
        default <T, E extends Exception> T fetch(
            DataSource ds, PreparedStatementFetcher<T, E> fetcher
        ) throws E {
            return TrueJdbc.withConnection(ds, cn -> fetch(cn, fetcher));
        }

        default <T, E extends Exception> T fetch(
            Connection cn, PreparedStatementFetcher<T, E> fetcher
        ) throws E {
            PreparedStatement stmt = null;
            return fetcher.fetch(stmt);
        }
    }

    public interface UpdateCount extends ToPreparedStatement {
        default <T, E extends Exception> UpdateResult<T> fetch(
            DataSource ds, PreparedStatementFetcher<T, E> fetcher
        ) throws E {
            return TrueJdbc.withConnection(ds, cn -> fetch(cn, fetcher));
        }

        default <T, E extends Exception> UpdateResult<T> fetch(
            Connection cn, PreparedStatementFetcher<T, E> fetcher
        ) throws E {
            var self = this;
            return WithUpdateCount.wrap(
                () -> ((Default) self::prepare).fetch(cn, fetcher)
            );
        }
    }
}
