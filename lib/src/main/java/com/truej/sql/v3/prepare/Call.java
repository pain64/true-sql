package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Call
    extends CallSettings<Call, Long>
    implements FetcherNone.Instance, FetcherOne.Instance,
    FetcherUpdateCount.Instance<Long>, FetcherManual.Instance {

    @Override Call self() { return this; }

    @Override void execute(CallableStatement stmt) throws SQLException {
        stmt.execute();
    }

    @Override public Long getUpdateCount(PreparedStatement stmt) throws SQLException {
        return stmt.getLargeUpdateCount();
    }
}