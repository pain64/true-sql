package net.truej.sql.compiler;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public abstract class GenericArrayRw<T> implements TypeReadWrite<T> {
    abstract T convert(Array array) throws SQLException;

    @Override public T get(ResultSet rs, int columnIndex) throws SQLException {
        return convert(rs.getArray(columnIndex));
    }

    @Override public void set(PreparedStatement stmt, int parameterIndex, T value) throws SQLException {
        stmt.setObject(parameterIndex, value);
    }

    @Override
    public T get(CallableStatement stmt, int parameterIndex) throws SQLException {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public void registerOutParameter(CallableStatement stmt, int parameterIndex) throws SQLException {
        throw new RuntimeException("NOT IMPLEMENTED");
    }
}
