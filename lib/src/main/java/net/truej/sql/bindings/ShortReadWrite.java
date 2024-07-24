package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class ShortReadWrite implements TypeReadWrite<java.lang.Short> {

    @Override public java.lang.Short get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getShort(columnIndex);
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.lang.Short value
    ) throws SQLException {
        if (value == null)
            stmt.setNull(parameterIndex, Types.SMALLINT);
        else
            stmt.setShort(parameterIndex, value);
    }

    @Override public java.lang.Short get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getShort(parameterIndex);
        if (stmt.wasNull())
            return null;

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.SMALLINT);
    }
}
