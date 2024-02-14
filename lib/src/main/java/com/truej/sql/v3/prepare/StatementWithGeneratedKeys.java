package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

public class StatementWithGeneratedKeys implements
    ToPreparedStatement,
    FetcherOne, FetcherOneOrNull, FetcherOneOptional,
    FetcherArray, FetcherList, FetcherStream, FetcherManual {

    // FIXME: create leaf StatementWithGeneratedKeysAndUpdateCount
    public <T> UpdateResult<T> withUpdateCount(
        Function<StatementWithGeneratedKeys, T> stmt
    ) {
        return withUpdateCount(false, stmt);
    }

    public <T> UpdateResult<T> withUpdateCount(
        boolean isLarge, Function<StatementWithGeneratedKeys, T> stmt
    ) {
        return null;
    }

    @Override public PreparedStatement prepare(Connection connection) {
        return null;
    }
}
