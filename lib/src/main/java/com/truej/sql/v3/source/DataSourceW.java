package com.truej.sql.v3.source;

import com.truej.sql.v3.fetch.UpdateResult;
import com.truej.sql.v3.source.Parameters.*;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public non-sealed interface DataSourceW extends Source {
    DataSource w();

    interface WithConnectionAction<T, E extends Exception> {
        T run(ConnectionW cn) throws E;
    }

    default <T, E extends Exception> T withConnection(
        WithConnectionAction<T, E> action
    ) throws E {
        var self = this;
        try (var cn = w().getConnection()) {
            return action.run(
                new ConnectionW() {
                    @Override public RuntimeException mapException(SQLException ex) {
                        return self.mapException(ex);
                    }
                    @Override public Connection w() {
                        return cn;
                    }
                }
            );
        } catch (SQLException e) {
            throw mapException(e);
        }
    }

    interface InTransactionAction<T, E extends Exception> {
        T run(ConnectionW connection) throws E;
    }

    default <T, E extends Exception> T inTransaction(
        InTransactionAction<T, E> action
    ) throws E {
        return withConnection(
            conn -> conn.inTransaction(() -> action.run(conn))
        );
    }

    interface FetchSimple {
        default <T> T delegated() { throw new RuntimeException("delegated"); }

        default Void fetchNone() { return delegated(); }


        default  <T> T fetchOne(Class<T> toClass) { return delegated(); }

        default <T> @Nullable T fetchOne(AsNullable asNullable, Class<T> toClass) { return delegated(); }

        default <T> T fetchOne(AsNotNull asNotNull, Class<T> toClass) { return delegated(); }


        default <T> @Nullable T fetchOneOrZero(Class<T> toClass) { return delegated(); }

        default <T> @Nullable T fetchOneOrZero(AsNullable asNullable, Class<T> toClass) { return delegated(); }

        default <T> @Nullable T fetchOneOrZero(AsNotNull asNotNull, Class<T> toClass) { return delegated(); }


        default <T> List<T> fetchList(Class<T> elementClass) { return delegated(); }

        default <T> List<@Nullable T> fetchList(AsNullable asNullable, Class<T> elementClass) { return delegated(); }

        default <T> List<T> fetchList(AsNotNull asNotNull, Class<T> elementClass) { return delegated(); }
    }

    interface FetchWithUpdateCount<U> {
        default <T> T delegated() { throw new RuntimeException("delegated"); }

        default U fetchNone() { return delegated(); }


        default  <T> UpdateResult<U, T> fetchOne(Class<T> toClass) { return delegated(); }

        default <T> UpdateResult<U, @Nullable T> fetchOne(AsNullable asNullable, Class<T> toClass) { return delegated(); }

        default <T> UpdateResult<U, T> fetchOne(AsNotNull asNotNull, Class<T> toClass) { return delegated(); }


        default <T> UpdateResult<U, @Nullable T> fetchOneOrZero(Class<T> toClass) { return delegated(); }

        default <T> UpdateResult<U, @Nullable T> fetchOneOrZero(AsNullable asNullable, Class<T> toClass) { return delegated(); }

        default <T> UpdateResult<U, @Nullable T> fetchOneOrZero(AsNotNull asNotNull, Class<T> toClass) { return delegated(); }


        default <T> UpdateResult<U, List<T>> fetchList(Class<T> elementClass) { return delegated(); }

        default <T> UpdateResult<U, List<@Nullable T>> fetchList(AsNullable asNullable, Class<T> elementClass) { return delegated(); }

        default <T> UpdateResult<U, List<T>> fetchList(AsNotNull asNotNull, Class<T> elementClass) { return delegated(); }
    }

    class Single implements ConnectionW.FetchSimple {

        public static class WithUpdateCount implements FetchWithUpdateCount<Long> {
            public final FetchWithUpdateCount<Long> g = this;
        }

        public static class AsCall implements ConnectionW.FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchSimple g = this;
        }

        public static class AsGeneratedKeys implements ConnectionW.FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchSimple g = this;
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final FetchSimple g = this;

        public AsCall asCall() { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(String... columnNames) { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(int... columnIndexes) { return delegated(); }
    }

    class Batched implements ConnectionW.FetchSimple {
        public static class WithUpdateCount implements FetchWithUpdateCount<long[]> {
            public final FetchWithUpdateCount<long[]> g = this;
        }

        public static class AsCall implements ConnectionW.FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchSimple g = this;
        }

        public static class AsGeneratedKeys implements ConnectionW.FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final ConnectionW.FetchSimple g = this;
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final FetchSimple g = this;

        public AsCall asCall() { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(String... columnNames) { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(int... columnIndexes) { return delegated(); }
    }

    default Single q(String query, Object... args) {
        throw new RuntimeException("delegated");
    }

    default <T> Batched q(List<T> batch, String query, BatchArgumentsExtractor<T> arguments) {
        throw new RuntimeException("delegated");
    }
}
