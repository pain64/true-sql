package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.CallableStatement;
import java.sql.SQLException;

public abstract class Call
    extends CallSettings<Call>
    implements ToPreparedStatement,
    FetcherNone.Instance, FetcherOne.Instance,
    FetcherUpdateCount.Instance, FetcherManual.Instance {

    @Override Call self() { return this; }

    @Override void execute(CallableStatement stmt) throws SQLException {
        stmt.execute();
    }
}