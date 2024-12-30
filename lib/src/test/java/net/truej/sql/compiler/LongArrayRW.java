package net.truej.sql.compiler;

import java.sql.*;
import java.util.Arrays;

public class LongArrayRW extends GenericArrayRw<long[]> {
    @Override long[] convert(Array array) throws SQLException {
        return Arrays.stream((Long[]) array.getArray()).mapToLong(v -> v).toArray();
    }
}
