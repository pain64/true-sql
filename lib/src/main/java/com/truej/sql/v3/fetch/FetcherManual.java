package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherManual {

    // FIXME: PreparedCall
    public interface PreparedStatementFetcher<T, E extends Exception> {
        // TODO: default preparedStatementMove: false
        T fetch(PreparedStatement stmt) throws E;
    }

    public interface Instance extends ToPreparedStatement {
        default <T, E extends Exception> T fetch(
            DataSource ds, PreparedStatementFetcher<T, E> fetcher
        ) throws E {
            return TrueSql.withConnection(ds, cn -> fetch(cn, fetcher));
        }

        default <T, E extends Exception> T fetch(
            Connection cn, PreparedStatementFetcher<T, E> fetcher
        ) throws E {
            try (var stmt = prepareAndExecute(cn)) {
                return fetcher.fetch(stmt);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }
}
