package net.truej.sql.compiler;

import java.sql.*;

public class IntArrayNullableRW extends GenericArrayRw<Integer[]> {
    @Override Integer[] convert(Array array) throws SQLException {
        return (Integer[]) array.getArray();
    }
}
