package com.truej.sql.v3.fetch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SomeLogic {
    public static ResultSet getResultSet(
        PreparedStatement stmt, boolean hasGeneratedKeys
    ) throws SQLException {
        return hasGeneratedKeys ? stmt.getGeneratedKeys() : stmt.getResultSet();
    }
}
