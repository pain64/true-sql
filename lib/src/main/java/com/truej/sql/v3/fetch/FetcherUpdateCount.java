package com.truej.sql.v3.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.TrueSql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherUpdateCount {
    public static class Hints { }

    public interface Next<T> {
        default boolean isPreparedStatementMoved() { return false; }
        T apply(PreparedStatement stmt);
    }

    public static <T> UpdateResult<T> fetch(PreparedStatement stmt, Next<T> next) {
        try {
            return new UpdateResult<>(
                // TODO: fallback from updateCount to updateCountLarge
                stmt.getUpdateCount(), next.apply(stmt)
            );
        } catch (SQLException e) {
            throw new SqlExceptionR(e);
        }
    }

    public interface Instance extends ToPreparedStatement {
        default <T> UpdateResult<T> fetchUpdateCount(DataSource ds, Next<T> next) {
            return TrueSql.withConnection(ds, cn -> fetchUpdateCount(cn, next));
        }

        default <T> UpdateResult<T> fetchUpdateCount(Connection cn, Next<T> next) {
            try (var stmt = prepareAndExecute(cn)) {
                return fetch(stmt, next);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }
}
