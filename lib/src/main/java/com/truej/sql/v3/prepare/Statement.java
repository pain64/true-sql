package com.truej.sql.v3.prepare;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Statement<P extends PreparedStatement>
    extends Base<Statement<P>, P, Void, Long> {

    @Override Long getUpdateCount(Void executionResult, P stmt) throws SQLException {
        return stmt.getLargeUpdateCount();
    }

    @Override Void execute(PreparedStatement stmt) throws SQLException {
        stmt.execute();
        return null;
    }
}
