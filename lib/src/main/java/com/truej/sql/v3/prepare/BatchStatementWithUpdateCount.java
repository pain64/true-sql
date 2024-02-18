package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;

public class BatchStatementWithUpdateCount implements ToPreparedStatement,
    FetcherNone.UpdateCount, FetcherManual.UpdateCount {

    @Override public PreparedStatement prepare() {
        return null;
    }
}