package net.truej.sql.fetch;

import java.sql.SQLException;

public class SqlExceptionR extends RuntimeException {
    public SqlExceptionR(SQLException cause) {
        super(cause);
    }
}
