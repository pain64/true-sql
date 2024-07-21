package net.truej.sql.source;

import net.truej.sql.SqlExceptionR;

import java.sql.SQLException;

public interface RuntimeConfig {
    default RuntimeException mapException(SQLException ex) {
        return new SqlExceptionR(ex);
    }
}
