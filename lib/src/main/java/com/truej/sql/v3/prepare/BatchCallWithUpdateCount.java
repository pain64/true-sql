package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;

public class BatchCallWithUpdateCount implements ToPreparedStatement,
    FetcherNone.UpdateCount, FetcherArray.UpdateCount,
    FetcherList.UpdateCount, FetcherStream.UpdateCount,
    FetcherManual.UpdateCount {

    @Override public PreparedStatement prepare() {
        return null;
    }
}