package net.truej.sql.source;

import net.truej.sql.fetch.As;
import net.truej.sql.fetch.NoUpdateCount;
import net.truej.sql.fetch.Q;
import net.truej.sql.fetch.UpdateCount;

import java.sql.Connection;
import java.sql.SQLException;

public non-sealed class ConnectionW implements
    Source, Q<ConnectionW.Single, ConnectionW.Batched> {

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

    public static class Single implements
        As<DataSourceW.AsCall, Single.AsGeneratedKeys>,
        NoUpdateCount.None, NoUpdateCount.One,
        NoUpdateCount.OneOrZero, NoUpdateCount.List_, NoUpdateCount.Stream_ {

        public static class G implements
            NoUpdateCount.None, NoUpdateCount.OneG,
            NoUpdateCount.OneOrZeroG, NoUpdateCount.ListG, NoUpdateCount.StreamG {}

        public static class WithUpdateCount implements
            UpdateCount.None<Long>, UpdateCount.One<Long>,
            UpdateCount.OneOrZero<Long>, UpdateCount.List_<Long>, UpdateCount.Stream_<Long> {

            public static class G implements
                UpdateCount.None<Long>, UpdateCount.OneG<Long>,
                UpdateCount.OneOrZeroG<Long>, UpdateCount.ListG<Long>, UpdateCount.StreamG<Long> { }

            public final G g = new G();
        }

        public static class AsGeneratedKeys implements
            NoUpdateCount.None, NoUpdateCount.One,
            NoUpdateCount.OneOrZero, NoUpdateCount.List_, NoUpdateCount.Stream_ {

            public static class WithUpdateCount implements
                UpdateCount.None<Long>, UpdateCount.One<Long>,
                UpdateCount.OneOrZero<Long>, UpdateCount.List_<Long>, UpdateCount.Stream_<Long> {

                public static class G implements
                    UpdateCount.None<Long>, UpdateCount.OneG<Long>,
                    UpdateCount.OneOrZeroG<Long>, UpdateCount.ListG<Long>, UpdateCount.StreamG<Long> { }

                public final G g = new G();
            }

            public static class G implements
                NoUpdateCount.None, NoUpdateCount.OneG,
                NoUpdateCount.OneOrZero, NoUpdateCount.List_, NoUpdateCount.Stream_ { }

            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final G g = new G();
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final G g = new G();
    }

    public static class Batched extends DataSourceW.Batched {}
}