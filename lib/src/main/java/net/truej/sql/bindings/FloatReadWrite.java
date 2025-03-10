package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class FloatReadWrite implements TypeReadWrite<Float> {
    @Override public Float get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getFloat(columnIndex);
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, Float value
    ) throws SQLException {
        if (value == null)
            stmt.setNull(parameterIndex, Types.REAL);
        else
            stmt.setFloat(parameterIndex, value);
    }

    @Override public Float get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getFloat(parameterIndex);
        if (stmt.wasNull())
            return null;

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.REAL);
    }
}
