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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (UpdateResultStream<?, ?>) o;
        return updateCount.equals(that.updateCount) && value.equals(that.value);
    }

    @Override public int hashCode() {
        var result = updateCount.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
