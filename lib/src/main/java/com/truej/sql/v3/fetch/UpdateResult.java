package com.truej.sql.v3.fetch;

public class UpdateResult<U, V> {
    public final U updateCount;
    public final V value;

    public UpdateResult(U updateCount, V value) {
        this.updateCount = updateCount;
        this.value = value;
    }
}
