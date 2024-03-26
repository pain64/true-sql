package com.truej.sql.v3.fetch;

// TODO: use lombok??? is equals / hashCode needed???
public class UpdateResult<T> {
    public final long updateCount;
    public final T value;

    public UpdateResult(long updateCount, T value) {
        this.updateCount = updateCount;
        this.value = value;
    }
}
