package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BatchCall
    extends CallSettings<BatchCall, long[]>
    implements FetcherNone.Instance, FetcherList.Instance,
    FetcherStream.Instance, FetcherUpdateCount.Instance<long[]>,
    FetcherManual.Instance {

    @Override BatchCall self() { return this; }

    private long[] updateCount;

    @Override void execute(CallableStatement stmt) throws SQLException {
        updateCount = stmt.executeLargeBatch();
    }

    @Override public long[] getUpdateCount(PreparedStatement stmt) {
        return updateCount;
    }
}
