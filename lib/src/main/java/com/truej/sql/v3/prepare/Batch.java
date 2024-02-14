package com.truej.sql.v3.prepare;

import java.util.List;
import java.util.stream.Stream;

public class Batch {
    @FunctionalInterface
    public interface StatementSupplier<T> {
        Statement supply(T element);
    }

    @FunctionalInterface
    public interface CallSupplier<T> {
        Call supply(T element);
    }

    public static <T> Statement stmt(T[] data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> Statement stmt(List<T> data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> Statement stmt(Stream<T> data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> Call call(T[] data, CallSupplier<T> query) {
        return null;
    }

    public static <T> Call call(List<T> data, CallSupplier<T> query) {
        return null;
    }

    public static <T> Call call(Stream<T> data, CallSupplier<T> query) {
        return null;
    }

    public static class BatchStatement { }
    public static class BatchStatementWithGeneratedKeys {}
    public static class BatchStatementWithGeneratedKeysAndRowsAffected {}

    public static class BatchCall { }
    public static class BatchCallWithRowsAffected {}
}
