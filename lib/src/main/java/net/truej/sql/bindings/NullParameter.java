package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NullParameter implements TypeReadWrite<Object> {

    @Override public Object get(ResultSet rs, int columnIndex) throws SQLException {
        throw new IllegalStateException("unreachable");
    }

    @Override public void set(PreparedStatement stmt, int parameterIndex, Object value) throws SQLException {
        stmt.setObject(parameterIndex, null);
    }

    @Override public Object get(CallableStatement stmt, int parameterIndex) throws SQLException {
        throw new IllegalStateException("unreachable");
    }

    @Override public void registerOutParameter(CallableStatement stmt, int parameterIndex) throws SQLException {
        throw new IllegalStateException("unreachable");
    }
}
