package net.truej.sql.fetch;

import java.util.Objects;

public class UpdateResult<U, V> {
    public final U updateCount;
    public final V value;

    public UpdateResult(U updateCount, V value) {
        this.updateCount = updateCount;
        this.value = value;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (UpdateResult<?, ?>) o;
        return updateCount.equals(that.updateCount) && Objects.equals(value, that.value);
    }

    @Override public int hashCode() {
        var result = updateCount.hashCode();
        result = 31 * result + Objects.hashCode(value);
        return result;
    }
}
