package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;

public class DateReadWrite implements TypeReadWrite<java.sql.Date> {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override public java.sql.Date get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getDate(columnIndex, Calendar.getInstance(UTC));
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.sql.Date value
    ) throws SQLException {
        
        
        stmt.setDate(parameterIndex, value, Calendar.getInstance(UTC));
    }

    @Override public java.sql.Date get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getDate(parameterIndex, Calendar.getInstance(UTC));
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
