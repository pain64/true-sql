package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeReadWrite implements TypeReadWrite<java.sql.Time> {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");


    @Override public java.sql.Time get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getTime(columnIndex, Calendar.getInstance(UTC));
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.sql.Time value
    ) throws SQLException {
        
        
        stmt.setTime(parameterIndex, value, Calendar.getInstance(UTC));
    }

    @Override public java.sql.Time get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getTime(parameterIndex, Calendar.getInstance(UTC));
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
