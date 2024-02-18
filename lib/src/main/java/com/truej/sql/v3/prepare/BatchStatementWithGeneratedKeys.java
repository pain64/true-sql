package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;

public class BatchStatementWithGeneratedKeys implements ToPreparedStatement,
    FetcherArray.Default, FetcherList.Default,
    FetcherStream.Default, FetcherManual.Default {

    public BatchStatementWithGeneratedKeysAndUpdateCount withUpdateCount() {
        return withUpdateCount(false);
    }

    public BatchStatementWithGeneratedKeysAndUpdateCount withUpdateCount(boolean isLarge) {
        return null;
    }

    @Override public PreparedStatement prepare() {
        return null;
    }
}