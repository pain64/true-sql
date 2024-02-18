package com.truej.sql.v3.fetch;

class WithUpdateCount {
    interface ValueProvider<T, E extends Exception> {
        T provide() throws E;
    }
    static <T, E extends Exception> UpdateResult<T> wrap(ValueProvider<T, E> result) throws E {
        // TODO
        return new UpdateResult<>(0, result.provide());
    }
}
