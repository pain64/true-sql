package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BatchStatement
    extends StatementSettings<BatchStatement, long[]>
    implements FetcherNone.Instance, FetcherList.Instance,
    FetcherStream.Instance, FetcherUpdateCount.Instance<long[]>,
    FetcherGeneratedKeys.Instance, FetcherManual.Instance {

    @Override BatchStatement self() { return this; }

    private long[] updateCount;

    @Override void execute(PreparedStatement stmt) throws SQLException {
        updateCount = stmt.executeLargeBatch();
    }

    @Override public long[] getUpdateCount(PreparedStatement stmt) {
        return updateCount;
    }
}