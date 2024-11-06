package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class DoubleReadWrite implements TypeReadWrite<Double> {
    @Override public Double get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getDouble(columnIndex);
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, Double value
    ) throws SQLException {
        if (value == null)
            stmt.setNull(parameterIndex, Types.DOUBLE);
        else
            stmt.setDouble(parameterIndex, value);
    }

    @Override public Double get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getDouble(parameterIndex);
        if (stmt.wasNull())
            return null;

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.DOUBLE);
    }
}
