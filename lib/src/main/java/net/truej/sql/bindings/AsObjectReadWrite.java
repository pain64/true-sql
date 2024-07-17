package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public abstract class AsObjectReadWrite<A> implements TypeReadWrite<A> {
    public abstract Class<A> aClass();
    boolean mayBeNull() { return true; }
    int sqlType() { return Types.OTHER; }

    @Override public A get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        return rs.getObject(columnIndex, aClass());
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, A value
    ) throws SQLException {
        stmt.setObject(parameterIndex, value);
    }

    @Override public A get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        return stmt.getObject(parameterIndex, aClass());
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, sqlType());
    }
}
