package net.truej.sql.compiler;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;


abstract class PgEnumRW<T extends Enum<T>> implements TypeReadWrite<T> {

    public abstract Class<T> aClass();

    @Override public T get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        return Enum.valueOf(aClass(), rs.getString(columnIndex));
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, T value
    ) throws SQLException {
        stmt.setObject(parameterIndex, value, Types.OTHER);
    }

    @Override public T get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        return Enum.valueOf(aClass(), stmt.getString(parameterIndex));
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.OTHER);
    }
}

