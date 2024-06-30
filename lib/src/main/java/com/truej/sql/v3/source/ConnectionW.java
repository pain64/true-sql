package com.truej.sql.v3.source;

import com.truej.sql.v3.fetch.UpdateResultStream;
import com.truej.sql.v3.source.Parameters.*;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

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

    interface FetchSimple extends DataSourceW.FetchSimple {

        default <T> Stream<T> fetchStream(Class<T> elementClass) { return delegated(); }

        default <T> Stream<@Nullable T> fetchStream(AsNullable asNullable, Class<T> elementClass) { return delegated(); }

        default <T> Stream<T> fetchStream(AsNotNull asNotNull, Class<T> elementClass) { return delegated(); }
    }

    interface FetchWithUpdateCount<U> extends DataSourceW.FetchWithUpdateCount<U> {

        default <T> UpdateResultStream<U, Stream<T>> fetchStream(Class<T> elementClass) { return delegated(); }

        default <T> UpdateResultStream<U,Stream<@Nullable T>> fetchStream(AsNullable asNullable, Class<T> elementClass) { return delegated(); }

        default <T> UpdateResultStream<U,Stream<T>> fetchStream(AsNotNull asNotNull, Class<T> elementClass) { return delegated(); }
    }

    class Single implements FetchSimple {

        public static class WithUpdateCount implements FetchWithUpdateCount<Long> {
            public final FetchWithUpdateCount<Long> g = this;
        }

        public static class AsCall implements FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchSimple g = this;
        }

        public static class AsGeneratedKeys implements FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchSimple g = this;
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final FetchSimple g = this;

        public AsCall asCall() { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(String... columnNames) { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(int... columnIndexes) { return delegated(); }
    }

    class Batched implements FetchSimple {
        public static class WithUpdateCount implements FetchWithUpdateCount<long[]> {
            public final FetchWithUpdateCount<long[]> g = this;
        }

        public static class AsCall implements FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchSimple g = this;
        }

        public static class AsGeneratedKeys implements FetchSimple {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchSimple g = this;
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

// TO BE DONE
//    interface BatchTemplateExtractor<T> {
//        StringTemplate extract(T one);
//    }
//
//    default void q(StringTemplate query) {
//        throw new RuntimeException("delegated");
//    }
//
//    default <T> void q(List<T> batch, BatchTemplateExtractor<T> query) {
//        throw new RuntimeException("delegated");
//    }
}
