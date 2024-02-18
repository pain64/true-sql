package com.truej.sql.v3.fetch;

public class UpdateResult<T> {
    public final long count;
    public final T value;

    public UpdateResult(long count, T value) {
        this.count = count;
        this.value = value;
    }
}
