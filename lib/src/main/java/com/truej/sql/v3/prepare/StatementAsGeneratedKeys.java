package com.truej.sql.v3.prepare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class StatementAsGeneratedKeys  extends Base.Single<StatementAsGeneratedKeys, PreparedStatement> {

    @Override PreparedStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override StatementAsGeneratedKeys self() { return this; }

    @Override Void execute(PreparedStatement stmt) throws SQLException {
        stmt.execute();
        return null;
    }
}
