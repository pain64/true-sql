package net.truej.sql.fetch;

public class UpdateResult<U, V> {
    public final U updateCount;
    public final V value;

    public UpdateResult(U updateCount, V value) {
        this.updateCount = updateCount;
        this.value = value;
    }

    @Override public String toString() {
        return "UpdateResult[" +
               "updateCount=" + updateCount +
               ", value=" + value +
               ']';
    }
}
