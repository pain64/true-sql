package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;
import java.util.function.Function;

public class BatchStatement implements ToPreparedStatement,
    FetcherNone.Default, FetcherArray.Default,
    FetcherList.Default, FetcherStream.Default,
    FetcherManual.Default {

    public <T> UpdateResult<T> withUpdateCount(
        Function<BatchStatementWithUpdateCount, T> stmt
    ) {
        return withUpdateCount(false, stmt);
    }

    public <T> UpdateResult<T> withUpdateCount(
        boolean isLarge, Function<BatchStatementWithUpdateCount, T> stmt
    ) {
        return null;
    }

    @Override public PreparedStatement prepare() {
        return null;
    }
}