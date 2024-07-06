package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class PrimitiveBooleanReadWrite implements TypeReadWrite<java.lang.Boolean> {

    @Override public java.lang.Boolean get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getBoolean(columnIndex);
        if (rs.wasNull())
            throw new IllegalStateException("null not expected");
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.Boolean value
    ) throws SQLException {
        if (value == null)
            throw new IllegalStateException("null not expected");
        stmt.setBoolean(parameterIndex, value);
    }

    @Override public java.lang.Boolean get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getBoolean(parameterIndex);
        if (stmt.wasNull())
            throw new IllegalStateException("null not expected");

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.BOOLEAN);
    }
}
