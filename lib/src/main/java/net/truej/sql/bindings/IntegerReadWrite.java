package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class IntegerReadWrite implements TypeReadWrite<java.lang.Integer> {

    @Override public java.lang.Integer get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getInt(columnIndex);
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.Integer value
    ) throws SQLException {
        
        
        stmt.setInt(parameterIndex, value);
    }

    @Override public java.lang.Integer get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getInt(parameterIndex);
        if (stmt.wasNull())
            return null;

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.INTEGER);
    }
}
