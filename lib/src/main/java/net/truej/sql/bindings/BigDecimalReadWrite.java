package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class BigDecimalReadWrite implements TypeReadWrite<java.math.BigDecimal> {

    @Override public java.math.BigDecimal get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getBigDecimal(columnIndex);
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, java.math.BigDecimal value
    ) throws SQLException {
        
        
        stmt.setBigDecimal(parameterIndex, value);
    }

    @Override public java.math.BigDecimal get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getBigDecimal(parameterIndex);
        if (stmt.wasNull())
            return null;

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.NUMERIC);
    }
}
