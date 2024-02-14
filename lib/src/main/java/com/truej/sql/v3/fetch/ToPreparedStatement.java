package com.truej.sql.v3.fetch;

import java.sql.Connection;
import java.sql.PreparedStatement;

public interface ToPreparedStatement {
    PreparedStatement prepare(Connection connection);
}
