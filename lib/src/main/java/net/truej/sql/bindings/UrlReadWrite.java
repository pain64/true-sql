package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class UrlReadWrite implements TypeReadWrite<java.net.URL> {

    @Override public java.net.URL get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getURL(columnIndex);
        
        
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.net.URL value
    ) throws SQLException {
        
        
        stmt.setURL(parameterIndex, value);
    }

    @Override public java.net.URL get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getURL(parameterIndex);
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
