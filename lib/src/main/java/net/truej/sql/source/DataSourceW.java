package net.truej.sql.source;

import net.truej.sql.fetch.As;
import net.truej.sql.fetch.NoUpdateCount;
import net.truej.sql.fetch.Q;
import net.truej.sql.fetch.UpdateCount;

import javax.sql.DataSource;
import java.sql.SQLException;

public non-sealed class DataSourceW implements
    Source, Q<DataSourceW.Single, DataSourceW.Batched> {

    public final DataSource w;

    public DataSourceW(DataSource w) {this.w = w;}

    public interface WithConnectionAction<T, E extends Exception> {
        T run(ConnectionW cn) throws E;
    }

    public final <T, E extends Exception> T withConnection(
        WithConnectionAction<T, E> action
    ) throws E {
        var self = this;
        try (var cn = w.getConnection()) {
            return action.run(
                new ConnectionW(cn) {
                    @Override public RuntimeException mapException(SQLException ex) {
                        return self.mapException(ex);
                    }
                }
            );
        } catch (SQLException e) {
            throw mapException(e);
        }
    }

    public interface InTransactionAction<T, E extends Exception> {
        T run(ConnectionW connection) throws E;
    }

    public final <T, E extends Exception> T inTransaction(
        InTransactionAction<T, E> action
    ) throws E {
        return withConnection(
            conn -> conn.inTransaction(() -> action.run(conn))
        );
    }

    public static class AsCall implements
        NoUpdateCount.None, NoUpdateCount.One {

        public static class WithUpdateCount implements
            UpdateCount.None<long[]>, UpdateCount.One<long[]> {

            public static class G implements
                UpdateCount.None<long[]>, UpdateCount.One<long[]> { }

            public final G g = new G();
        }

        public static class G implements
            NoUpdateCount.None, NoUpdateCount.OneG { }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final G g = new G();
    }

    public static class Single implements
        As<AsCall, Single.AsGeneratedKeys>,
        NoUpdateCount.None, NoUpdateCount.One,
        NoUpdateCount.OneOrZero, NoUpdateCount.List_ {

        public static class G implements
            NoUpdateCount.None, NoUpdateCount.OneG,
            NoUpdateCount.OneOrZeroG, NoUpdateCount.ListG {}

        public static class WithUpdateCount implements
            UpdateCount.None<Long>, UpdateCount.One<Long>,
            UpdateCount.OneOrZero<Long>, UpdateCount.List_<Long> {

            public static class G implements
                UpdateCount.None<Long>, UpdateCount.OneG<Long>,
                UpdateCount.OneOrZeroG<Long>, UpdateCount.ListG<Long> { }

            public final G g = new G();
        }

        public static class AsGeneratedKeys implements
            NoUpdateCount.None, NoUpdateCount.One,
            NoUpdateCount.OneOrZero, NoUpdateCount.List_ {

            public static class WithUpdateCount implements
                UpdateCount.None<Long>, UpdateCount.One<Long>,
                UpdateCount.OneOrZero<Long>, UpdateCount.List_<Long> {

                public static class G implements
                    UpdateCount.None<Long>, UpdateCount.OneG<Long>,
                    UpdateCount.OneOrZeroG<Long>, UpdateCount.ListG<Long> { }

                public final G g = new G();
            }

            public static class G implements
                NoUpdateCount.None, NoUpdateCount.OneG,
                NoUpdateCount.OneOrZero, NoUpdateCount.List_ { }

            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final G g = new G();
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final G g = new G();
    }

    public static class Batched implements
        As<AsCall, Batched.AsGeneratedKeys>,
        NoUpdateCount.None, NoUpdateCount.List_ {

        public static class AsGeneratedKeys implements
            NoUpdateCount.None, NoUpdateCount.List_ {

            public static class WithUpdateCount implements
                UpdateCount.None<long[]>, UpdateCount.List_<long[]> {

                public static class G implements
                    UpdateCount.None<long[]>, UpdateCount.ListG<long[]> { }

                public final G g = new G();
            }

            public final WithUpdateCount withUpdateCount = new WithUpdateCount();

            public static class G implements
                NoUpdateCount.None, NoUpdateCount.OneG { }

            public final G g = new G();
        }

        public static class WithUpdateCount implements
            UpdateCount.None<long[]>, UpdateCount.List_<long[]> {

            public static class G implements
                UpdateCount.None<long[]>, UpdateCount.ListG<long[]> { }

            public final G g = new G();
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();

        public static class G implements
            NoUpdateCount.None, NoUpdateCount.ListG {}

        public final G g = new G();
    }
}