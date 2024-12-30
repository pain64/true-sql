package net.truej.sql.compiler;

import java.sql.*;

public class FloatArrayRW extends GenericArrayRw<float[]> {
    @Override
    float[] convert(Array array) throws SQLException {
        var sa = (Float[]) array.getArray();
        var result = new float[sa.length];
        for (var i = 0; i < sa.length; i++)
            result[i] = sa[i];
        return result;
    }
}
