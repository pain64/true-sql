package net.truej.sql.compiler;

import java.sql.*;

public class BooleanArrayRW extends GenericArrayRw<boolean[]> {
    @Override
    boolean[] convert(Array array) throws SQLException {
        var sa = (Boolean[]) array.getArray();
        var result = new boolean[sa.length];
        for (var i = 0; i < sa.length; i++)
            result[i] = sa[i];
        return result;
    }
}
