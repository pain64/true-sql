package net.truej.sql.source;

import net.truej.sql.fetch.UpdateResultStream;
import net.truej.sql.source.Parameters.*;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public interface ConnectionW extends RuntimeConfig {
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

    interface FetchStatementG extends DataSourceW.FetchStatement {
        default <T> Stream<T> fetchStream(Class<T> elementClass) { return delegated(); }
    }

    interface FetchStatement extends FetchStatementG {

        default <T> Stream<@Nullable T> fetchStream(AsNullable asNullable, Class<T> elementClass) { return delegated(); }

        default <T> Stream<T> fetchStream(AsNotNull asNotNull, Class<T> elementClass) { return delegated(); }
    }

    interface FetchWithUpdateCountG<U> extends DataSourceW.FetchStatementWithUpdateCount<U> {
        default <T> UpdateResultStream<U, Stream<T>> fetchStream(Class<T> elementClass) { return delegated(); }
    }

    interface FetchStatementWithUpdateCount<U> extends FetchWithUpdateCountG<U> {

        default <T> UpdateResultStream<U, Stream<@Nullable T>> fetchStream(AsNullable asNullable, Class<T> elementClass) { return delegated(); }

        default <T> UpdateResultStream<U, Stream<T>> fetchStream(AsNotNull asNotNull, Class<T> elementClass) { return delegated(); }
    }

    class Single implements FetchStatement {

        public static class WithUpdateCount implements FetchStatementWithUpdateCount<Long> {
            public final FetchWithUpdateCountG<Long> g = this;
        }

        public static class AsCall implements DataSourceW.FetchCall {
            // FIXME - make custom update count ???
            // FIXME - make all as abstract classes ???
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final DataSourceW.FetchCallG g = this;
        }

        public static class AsGeneratedKeys implements FetchStatement {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchStatementG g = this;
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final FetchStatementG g = this;

        public AsCall asCall() { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(String... columnNames) { return delegated(); }

        public AsGeneratedKeys asGeneratedKeys(int... columnIndexes) { return delegated(); }
    }

    class Batched implements FetchStatement {
        public static class WithUpdateCount implements FetchStatementWithUpdateCount<long[]> {
            public final FetchWithUpdateCountG<long[]> g = this;
        }

        public static class AsCall implements FetchStatement {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchStatementG g = this;
        }

        public static class AsGeneratedKeys implements FetchStatement {
            public final WithUpdateCount withUpdateCount = new WithUpdateCount();
            public final FetchStatementG g = this;
        }

        public final WithUpdateCount withUpdateCount = new WithUpdateCount();
        public final FetchStatementG g = this;

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
