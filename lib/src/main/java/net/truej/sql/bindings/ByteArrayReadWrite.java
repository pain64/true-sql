package net.truej.sql.bindings;

import net.truej.sql.config.TypeReadWrite;

import java.sql.*;

public class ByteArrayReadWrite implements TypeReadWrite<byte[]> {

    @Override public byte[] get(
        ResultSet rs, int columnIndex
    ) throws SQLException {
        var value = rs.getBytes(columnIndex);
        if (rs.wasNull())
            return null;
        return value;
    }

    @Override public void set(
        PreparedStatement stmt, int parameterIndex, byte[] value
    ) throws SQLException {
        
        
        stmt.setBytes(parameterIndex, value);
    }

    @Override public byte[] get(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        var v = stmt.getBytes(parameterIndex);
        if (stmt.wasNull())
            return null;

        return v;
    }

    @Override public void registerOutParameter(
        CallableStatement stmt, int parameterIndex
    ) throws SQLException {
        stmt.registerOutParameter(parameterIndex, Types.VARBINARY);
    }
}
