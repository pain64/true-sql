package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;

public class TimestampReadWrite implements TypeReadWrite<java.sql.Timestamp> {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override public java.sql.Timestamp get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getTimestamp(columnIndex, Calendar.getInstance(UTC));
        if (rs.wasNull())
            return null;
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
        var v = stmt.getTimestamp(parameterIndex, Calendar.getInstance(UTC));
        if (stmt.wasNull())
            return null;

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.VARCHAR);
    }
}
