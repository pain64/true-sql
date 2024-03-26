package com.truej.sql.v3.config;

import com.truej.sql.v3.SqlExceptionR;

import java.sql.SQLException;

// FIXME: is interface OK???
public interface ExceptionMapper {
    default RuntimeException map(SQLException e) {
        return new SqlExceptionR(e);
    }
}
