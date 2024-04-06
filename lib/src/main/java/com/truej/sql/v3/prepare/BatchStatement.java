package com.truej.sql.v3.prepare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BatchStatement
    extends Base.Batch<BatchStatement, PreparedStatement>
    implements With.GeneratedKeysConstructors<BatchStatement> {

    @Override PreparedStatement defaultConstructor(
        Connection connection, String sql
    ) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override BatchStatement self() { return this; }

    @Override long[] execute(PreparedStatement stmt) throws SQLException {
        return stmt.executeLargeBatch();
    }
}