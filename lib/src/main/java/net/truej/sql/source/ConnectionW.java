package net.truej.sql.source;
import net.truej.sql.fetch.Q;

import java.sql.Connection;
import java.sql.SQLException;

public non-sealed class ConnectionW implements
    Source, Q<DataSourceW.Single, DataSourceW.Batched> {

    public final Connection w;
    public ConnectionW(Connection w) { this.w = w; }

    public interface InTransactionAction<T, E extends Exception> {
        T run() throws E;
    }

    public final <T, E extends Exception> T inTransaction(
        InTransactionAction<T, E> action
    ) throws E {
        try {
            var oldAutoCommit = w.getAutoCommit();

            try {
                w.setAutoCommit(false);
                var result = action.run();
                w.commit();
                return result;
            } catch (Throwable e) {
                w.rollback();
                throw e;
            } finally {
                w.setAutoCommit(oldAutoCommit);
            }
        } catch (SQLException e) {
            throw mapException(e);
        }
    }
}