package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class PrimitiveByteReadWrite implements TypeReadWrite<java.lang.Byte> {

    @Override public java.lang.Byte get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getByte(columnIndex);
        if (rs.wasNull())
            throw new IllegalStateException("null not expected");
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.Byte value
    ) throws SQLException {
        if (value == null)
            throw new IllegalStateException("null not expected");
        stmt.setByte(parameterIndex, value);
    }

    @Override public java.lang.Byte get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getByte(parameterIndex);
        if (stmt.wasNull())
            throw new IllegalStateException("null not expected");

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.TINYINT);
    }
}