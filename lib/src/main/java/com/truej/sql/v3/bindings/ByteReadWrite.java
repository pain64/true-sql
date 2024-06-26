package com.truej.sql.v3.bindings;

import com.truej.sql.v3.config.TypeReadWrite;

import java.sql.*;

public class ByteReadWrite implements TypeReadWrite<java.lang.Byte> {

    @Override public java.lang.Byte get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getByte(columnIndex);
        
        
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.Byte value
    ) throws SQLException {
        
        
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
