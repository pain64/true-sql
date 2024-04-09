package com.truej.sql.v3.prepare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Statement
    extends Base.Single<Statement, PreparedStatement>
    implements With.GeneratedKeysConstructors<Statement>, Batchable {

    @Override PreparedStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override Statement self() { return this; }

    @Override Void execute(PreparedStatement stmt) throws SQLException {
        stmt.execute();
        return null;
    }
}
