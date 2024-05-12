package com.truej.sql.v3.prepare;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BatchStatement<P extends PreparedStatement>
    extends Base<BatchStatement<P>, P, long[], long[]> {

    @Override long[] getUpdateCount(long[] executionResult, P stmt) {
        return executionResult;
    }

    @Override long[] execute(PreparedStatement stmt) throws SQLException {
        return stmt.executeLargeBatch();
    }
}