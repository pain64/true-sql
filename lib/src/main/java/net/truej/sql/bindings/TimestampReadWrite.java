package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class TimestampReadWrite implements TypeReadWrite<java.sql.Timestamp> {

    @Override public java.sql.Timestamp get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getTimestamp(columnIndex);
        
        
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.sql.Timestamp value
    ) throws SQLException {
        
        
        stmt.setTimestamp(parameterIndex, value);
    }

    @Override public java.sql.Timestamp get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getTimestamp(parameterIndex);
        if (stmt.wasNull())
            throw new IllegalStateException("null not expected");

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.VARCHAR);
    }
}
