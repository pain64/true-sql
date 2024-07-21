package net.truej.sql.dsl;

import static net.truej.sql.dsl.MissConfigurationException.*;

public interface As<C, K> {
    default C asCall() { return raise(); }
    default K asGeneratedKeys(String... columnNames) { return raise(); }
    default K asGeneratedKeys(int... columnIndexes) { return raise(); }
}
