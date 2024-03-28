package com.truej.sql.v3.fetch;

public class UpdateResult<U, V> {
    public final U updateCount;
    public final V value;

    public UpdateResult(U updateCount, V value) {
        this.updateCount = updateCount;
        this.value = value;
    }

    public static class UpdateResultAutoClosable<U, V> implements AutoCloseable {
        public final U updateCount;
        public final V value;

        public UpdateResultAutoClosable(U updateCount, V value) {
            this.updateCount = updateCount;
            this.value = value;
        }

        @Override public void close() throws Exception {
            if (value instanceof AutoCloseable cl) cl.close();
        }
    }

    public UpdateResultAutoClosable<U, V> autoClosable() {
        return new UpdateResultAutoClosable<>(updateCount, value);
    }
}
