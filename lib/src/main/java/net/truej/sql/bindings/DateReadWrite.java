package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class DateReadWrite implements TypeReadWrite<java.sql.Date> {

    @Override public java.sql.Date get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getDate(columnIndex);
        
        
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.sql.Date value
    ) throws SQLException {
        
        
        stmt.setDate(parameterIndex, value);
    }

    @Override public java.sql.Date get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getDate(parameterIndex);
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
