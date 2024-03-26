package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherManual {
    // classify: From getGeneratedKeys() or getResultSet()

    // FIXME: PreparedCall
    public interface PreparedStatementFetcher<T, E extends Exception> {
        // TODO: default preparedStatementMove: false
        T fetch(PreparedStatement stmt) throws E;
    }

    public interface Instance extends ToPreparedStatement {
        default <T, E extends Exception> T fetch(
            Source source, PreparedStatementFetcher<T, E> fetcher
        ) throws E {
            // TODO: решать как делать execute нужно в самом Fetcher
            return source.withConnection(cn -> {
                try (var stmt = prepareAndExecute(cn.w())) {
                    return fetcher.fetch(stmt);
                } catch (SQLException e) {
                    throw mapException(e);
                }
            });
        }
    }
}
