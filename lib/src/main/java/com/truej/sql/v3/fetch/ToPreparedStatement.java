package com.truej.sql.v3.fetch;

import com.truej.sql.v3.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ToPreparedStatement {
    PreparedStatement prepareAndExecute(Connection cn) throws SQLException;
    // execute vs executeUpdate -> T (long, int[])
    RuntimeException mapException(SQLException e);

    interface ManagedAction<T> {
        T apply(PreparedStatement stmt) throws SQLException;
    }

    default <T> T managed(
        Source source, Supplier<Boolean> willBeMoved, ManagedAction<T> action
    ) {
        return source.withConnection(cn -> {
            try {
                var triedToClose = false;
                var stmt = prepareAndExecute(cn.w());

                try {
                    return action.apply(stmt);
                } catch (Exception e) {
                    triedToClose = true;
                    try {
                        stmt.close();
                    } catch (Exception e2) {
                        e.addSuppressed(e2);
                    }

                    throw e;
                } finally {
                    if (!triedToClose && !willBeMoved.get())
                        stmt.close();
                }
            } catch (SQLException e) {
                throw mapException(e);
            }
        });
    }
}
