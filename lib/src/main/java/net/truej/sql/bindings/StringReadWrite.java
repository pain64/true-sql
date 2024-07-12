package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class StringReadWrite implements TypeReadWrite<java.lang.String> {

    @Override public java.lang.String get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getString(columnIndex);
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.String value
    ) throws SQLException {
        
        
        stmt.setString(parameterIndex, value);
    }

    @Override public java.lang.String get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getString(parameterIndex);
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
