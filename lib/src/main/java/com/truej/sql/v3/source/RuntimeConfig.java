package com.truej.sql.v3.source;

import com.truej.sql.v3.SqlExceptionR;

import java.sql.SQLException;

public interface RuntimeConfig {
    default RuntimeException mapException(SQLException ex) {
        return new SqlExceptionR(ex);
    }
}
