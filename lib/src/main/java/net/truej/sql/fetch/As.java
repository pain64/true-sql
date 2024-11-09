package net.truej.sql.fetch;

import static net.truej.sql.fetch.MissConfigurationException.*;

public interface As<C, K> {
    default C asCall() { return raise(); }
    default K asGeneratedKeys(String... columnNames) { return raise(); }
    default K asGeneratedKeys(int... columnIndexes) { return raise(); }
}
