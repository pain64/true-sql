package com.truej.sql.v3.source;

import com.truej.sql.v3.prepare.Statement;

import java.sql.Connection;
import java.sql.SQLException;

public non-sealed interface ConnectionW extends Source {
    Connection w();

    interface InTransactionAction<T, E extends Exception> {
        T run() throws E;
    }

    @SuppressWarnings("resource") default <T, E extends Exception> T inTransaction(
        InTransactionAction<T, E> action
    ) throws E {
        try {
            var oldAutoCommit = w().getAutoCommit();

            try {
                w().setAutoCommit(false);
                var result = action.run();
                w().commit();
                return result;
            } catch (Throwable e) {
                w().rollback();
                throw e;
            } finally {
                w().setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            throw mapException(e);
        }
    }
}
