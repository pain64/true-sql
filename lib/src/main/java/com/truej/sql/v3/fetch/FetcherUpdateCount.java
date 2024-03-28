package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;
import com.truej.sql.v3.prepare.BatchCall;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherUpdateCount {
    public static class Hints { }

    public interface Next<T> {
        boolean willPreparedStatementBeMoved();
        T apply(PreparedStatement stmt) throws SQLException;
    }

    public interface Instance<U> extends ToPreparedStatement {
        U getUpdateCount(PreparedStatement stmt) throws SQLException;

        default <V> UpdateResult<U, V> fetchUpdateCount(
            Source source, Next<V> next
        ) {
            var self = this;

            return managed(
                source, new ManagedAction<>() {
                    @Override public boolean willPreparedStatementBeMoved() {
                        return next.willPreparedStatementBeMoved();
                    }
                    @Override public UpdateResult<U, V> apply(
                        PreparedStatement stmt
                    ) throws SQLException {
                        // a little FP-style
                        switch (self) {
                            case BatchCall bc -> bc.
                        }
                                                return new UpdateResult<>(getUpdateCount(stmt), next.apply(stmt));
                    }
                }
            );
        }

        default UpdateResult<U, Void> fetchUpdateCount(Source source) {
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
