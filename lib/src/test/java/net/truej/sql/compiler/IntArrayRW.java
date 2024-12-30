package net.truej.sql.compiler;

import java.sql.*;
import java.util.Arrays;

public class IntArrayRW extends GenericArrayRw<int[]> {
    @Override int[] convert(Array array) throws SQLException{
        return Arrays.stream((Integer[]) array.getArray()).mapToInt(v -> v).toArray();
    }
}
