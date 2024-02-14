package com.truej.sql.showcase;

import java.util.List;

public class NewApi {
    public interface Prepare {}
    public interface Executor<P extends Prepare, T> {}

    public static class PrepareStatement implements Prepare {

    }

    public static class PrepareBatchStatement implements Prepare {

    }

    public static class ListRows<T> implements Executor<PrepareStatement, List<T>> {

    }

    public static class One<T> implements Executor<PrepareStatement, T> {

    }

    public static  <T> One<T> one(Class<T> tClass) { return null; }

    public static <T, P extends Prepare> T fetch(Object database, P prepare, Executor<P, T> executor) {
        return null;
    }
}
