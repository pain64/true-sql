package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class PrimitiveShortReadWrite implements TypeReadWrite<java.lang.Short> {

    @Override public java.lang.Short get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getShort(columnIndex);
        if (rs.wasNull())
            throw new IllegalStateException("null not expected");
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.Short value
    ) throws SQLException {
        if (value == null)
            throw new IllegalStateException("null not expected");
        stmt.setShort(parameterIndex, value);
    }

    @Override public java.lang.Short get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getShort(parameterIndex);
        if (stmt.wasNull())
            throw new IllegalStateException("null not expected");

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.SMALLINT);
    }
}
