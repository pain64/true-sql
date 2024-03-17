package com.truej.sql.v3;

import java.sql.SQLException;

public class SqlExceptionR extends RuntimeException {
    public SqlExceptionR(SQLException cause) {
        super(cause);
    }
}
