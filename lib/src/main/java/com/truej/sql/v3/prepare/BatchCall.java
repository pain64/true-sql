package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;

public class BatchCall implements ToPreparedStatement,
    FetcherNone.Default, FetcherArray.Default,
    FetcherList.Default, FetcherStream.Default,
    FetcherManual.Default {

    public BatchCallWithUpdateCount withUpdateCount() {
        return withUpdateCount(false);
    }

    public BatchCallWithUpdateCount withUpdateCount(boolean isLarge) {
        return null;
    }

    @Override public PreparedStatement prepare() {
        return null;
    }
}
