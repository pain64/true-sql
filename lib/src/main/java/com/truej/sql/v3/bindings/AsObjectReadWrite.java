package com.truej.sql.v3.bindings;

import com.truej.sql.v3.config.TypeReadWrite;

import java.sql.*;

public class AsObjectReadWrite<A> implements TypeReadWrite<A> {
    boolean mayBeNull() { return true; }
    int sqlType() { return Types.OTHER; }

    @Override public A get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        return (A) rs.getObject(columnIndex);
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, A value
    ) throws SQLException {
        stmt.setObject(parameterIndex, value);
    }

    @Override public A get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        return (A) stmt.getObject(parameterIndex);
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, sqlType());
    }
}
