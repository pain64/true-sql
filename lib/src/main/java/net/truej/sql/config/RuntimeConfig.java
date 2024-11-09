package net.truej.sql.config;

import net.truej.sql.fetch.SqlExceptionR;

import java.sql.SQLException;

public interface RuntimeConfig {
    default RuntimeException mapException(SQLException ex) {
        return new SqlExceptionR(ex);
    }
}
