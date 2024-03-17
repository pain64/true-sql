package com.truej.sql.v3.fetch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ToPreparedStatement {
    PreparedStatement prepareAndExecute(Connection cn) throws SQLException;
}
