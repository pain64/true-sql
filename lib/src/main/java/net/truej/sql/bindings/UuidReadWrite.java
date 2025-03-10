package net.truej.sql.bindings;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class UuidReadWrite extends AsObjectReadWrite<UUID> {
    @Override public Class<UUID> aClass() { return UUID.class; }
    @Override public int sqlType() { return Types.OTHER; }
    // workaround for PG
    @Override public UUID get(CallableStatement stmt, int parameterIndex) throws SQLException {
        return stmt.getClass().getName().equals("org.postgresql.jdbc.PgCallableStatement")
            ? (UUID) stmt.getObject(parameterIndex)
            : stmt.getObject(parameterIndex, aClass());
    }
}
