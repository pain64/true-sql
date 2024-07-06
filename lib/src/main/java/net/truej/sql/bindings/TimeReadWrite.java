package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class TimeReadWrite implements TypeReadWrite<java.sql.Time> {

    @Override public java.sql.Time get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getTime(columnIndex);
        
        
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.sql.Time value
    ) throws SQLException {
        
        
        stmt.setTime(parameterIndex, value);
    }

    @Override public java.sql.Time get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getTime(parameterIndex);
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
