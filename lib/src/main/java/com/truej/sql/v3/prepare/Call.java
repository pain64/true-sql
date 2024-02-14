package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.FetcherManual;
import com.truej.sql.v3.fetch.FetcherNone;
import com.truej.sql.v3.fetch.FetcherOne;
import com.truej.sql.v3.fetch.ToPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

public final class Call implements
    ToPreparedStatement, FetcherNone, FetcherOne, FetcherManual {

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

    @Override public PreparedStatement prepare(Connection connection) {
        return null;
    }

}
