package net.truej.sql.compiler;

import java.sql.*;

public class ShortArrayRW extends GenericArrayRw<short[]> {
    @Override
    short[] convert(Array array) throws SQLException {
        var sa = (Short[]) array.getArray();
        var result = new short[sa.length];
        for (var i = 0; i < sa.length; i++)
            result[i] = sa[i];
        return result;
    }
}
