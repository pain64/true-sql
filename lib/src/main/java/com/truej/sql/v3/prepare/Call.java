package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;
import java.util.function.Function;

public final class Call implements ToPreparedStatement,
    FetcherNone.Default, FetcherOne.Default, FetcherManual.Default {

    @Override public PreparedStatement prepare() {
        return null;
    }

    public <T> UpdateResult<T> withUpdateCount(
        Function<CallWithUpdateCount, T> stmt
    ) {
        return withUpdateCount(false, stmt);
    }

    public <T> UpdateResult<T> withUpdateCount(
        boolean isLarge, Function<CallWithUpdateCount, T> stmt
    ) {
        return null;
    }
}
