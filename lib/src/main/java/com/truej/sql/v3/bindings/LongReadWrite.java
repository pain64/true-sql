package com.truej.sql.v3.bindings;

import com.truej.sql.v3.config.TypeReadWrite;

import java.sql.*;

public class LongReadWrite implements TypeReadWrite<java.lang.Long> {

    @Override public java.lang.Long get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getLong(columnIndex);
        
        
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.Long value
    ) throws SQLException {
        
        
        stmt.setLong(parameterIndex, value);
    }

    @Override public java.lang.Long get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getLong(parameterIndex);
        if (stmt.wasNull())
            throw new IllegalStateException("null not expected");

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.BIGINT);
    }
}
