package net.truej.sql.fetch;

import java.util.stream.Stream;

public class UpdateResultStream<U, V> implements AutoCloseable {
    public final U updateCount;
    public final Stream<V> value;

    public UpdateResultStream(U updateCount, Stream<V> value) {
        this.updateCount = updateCount;
        this.value = value;
    }

    @Override public void close() {
        value.close();
    }
}
