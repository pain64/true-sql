package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BatchStatement
    extends StatementSettings<BatchStatement>
    implements ToPreparedStatement,
    FetcherNone.Instance, FetcherList.Instance, FetcherStream.Instance,
    FetcherUpdateCount.Instance, FetcherGeneratedKeys.Instance, FetcherManual.Instance {

    @Override BatchStatement self() { return this; }

    @Override void execute(PreparedStatement stmt) throws SQLException {
        stmt.executeBatch();
    }
}