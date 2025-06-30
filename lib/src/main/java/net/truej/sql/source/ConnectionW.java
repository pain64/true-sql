package net.truej.sql.source;

import net.truej.sql.fetch.Q;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public non-sealed class ConnectionW implements
    Source, Q<DataSourceW.Single, DataSourceW.Batched> {

    public final Connection w;
    public ConnectionW(Connection w) { this.w = w; }

    public interface InTransactionAction<T, E extends Exception> {
        T run() throws E;
    }

    private <T, E extends Exception> T inTransaction(
        @Nullable Integer isolationLevel, InTransactionAction<T, E> action
    ) throws E {
        try {
            var oldIsolationLevel = w.getTransactionIsolation();
            var oldAutoCommit = w.getAutoCommit();

            try {
                if (isolationLevel != null)
                    w.setTransactionIsolation(isolationLevel);
                w.setAutoCommit(false);

                var result = action.run();
                w.commit();
                return result;
            } catch (Throwable e) {
                w.rollback();
                throw e;
            } finally {
                if (isolationLevel != null)
                    w.setTransactionIsolation(oldIsolationLevel);
                w.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            throw mapException(e);
        }
    }

    public final <T, E extends Exception> T inTransaction(
        IsolationLevel isolationLevel, InTransactionAction<T, E> action
    ) throws E {
        return inTransaction(isolationLevel.jdbcCode(), action);
    }

    public final <T, E extends Exception> T inTransaction(
        InTransactionAction<T, E> action
    ) throws E {
        return inTransaction((Integer) null, action);
    }
}