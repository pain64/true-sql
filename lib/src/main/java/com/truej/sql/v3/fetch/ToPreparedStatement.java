package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ToPreparedStatement<R> {
    PreparedStatement prepare(Connection cn) throws SQLException;
    R execute(PreparedStatement stmt) throws SQLException;
    RuntimeException mapException(SQLException e);

    interface ManagedAction<R, T> {
        boolean willPreparedStatementBeMoved();
        T apply(PreparedStatement stmt) throws SQLException;
    }

    default <T> T managed(
        Source source, ManagedAction<R, T> action
    ) {
        return source.withConnection(cn -> {
            try {
                var triedToClose = false;
                var stmt = prepare(cn.w());
                var execResult = execute(stmt);

                try {
                    return action.apply(execResult, stmt);
                } catch (Exception e) {
                    triedToClose = true;
                    try {
                        stmt.close();
                    } catch (Exception e2) {
                        e.addSuppressed(e2);
                    }

                    throw e;
                } finally {
                    if (!triedToClose && !action.willPreparedStatementBeMoved())
                        stmt.close();
                }
            } catch (SQLException e) {
                throw mapException(e);
            }
        });
    }
}
