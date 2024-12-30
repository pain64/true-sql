package net.truej.sql.compiler;

import java.sql.*;
    import java.util.Arrays;

public class DoubleArrayRW extends GenericArrayRw<double[]> {
    @Override double[] convert(Array array) throws SQLException {
        return Arrays.stream((Double[]) array.getArray()).mapToDouble(v -> v).toArray();
    }
}
