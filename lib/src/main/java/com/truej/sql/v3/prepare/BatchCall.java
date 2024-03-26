package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.CallableStatement;
import java.sql.SQLException;

public abstract class BatchCall
    extends CallSettings<BatchCall>
    implements ToPreparedStatement,
    FetcherNone.Instance, FetcherList.Instance, FetcherStream.Instance,
    FetcherUpdateCount.Instance, FetcherManual.Instance {

    @Override BatchCall self() { return this; }

    @Override void execute(CallableStatement stmt) throws SQLException {
        stmt.executeBatch();
    }
}
