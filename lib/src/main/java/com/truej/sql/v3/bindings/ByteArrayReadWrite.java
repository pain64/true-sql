package com.truej.sql.v3.bindings;

import com.truej.sql.v3.config.TypeReadWrite;

import java.sql.*;

public class ByteArrayReadWrite implements TypeReadWrite<byte[]> {

    @Override public byte[] get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getBytes(columnIndex);
        
        
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, byte[] value
    ) throws SQLException {
        
        
        stmt.setBytes(parameterIndex, value);
    }

    @Override public byte[] get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getBytes(parameterIndex);
        if (stmt.wasNull())
            throw new IllegalStateException("null not expected");

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.VARBINARY);
    }
}
