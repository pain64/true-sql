package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherUpdateCount {
    public static class Hints { }

    public interface Next<T> {
        boolean willPreparedStatementBeMoved();
        T apply(PreparedStatement stmt) throws SQLException;
    }

    public static <T> UpdateResult<T> apply(
        PreparedStatement stmt, Next<T> next
    ) throws SQLException {
        return new UpdateResult<>(stmt.getLargeUpdateCount(), next.apply(stmt));
    }

    public interface Instance extends ToPreparedStatement {
        default <T> UpdateResult<T> fetchUpdateCount(
            Source source, Next<T> next
        ) {
            return managed(
                source, next::willPreparedStatementBeMoved, stmt -> apply(stmt, next)
            );
        }

        default UpdateResult<Void> fetchUpdateCount(Source source) {
            return fetchUpdateCount(source, new Next<>() {
                @Override public boolean willPreparedStatementBeMoved() {
                    return false;
                }
                @Override public Void apply(PreparedStatement stmt) {
                    return null;
                }
            });
        }
    }

}
