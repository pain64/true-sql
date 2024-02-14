package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

public class BatchStatementWithGeneratedKeys implements
    ToPreparedStatement, FetcherArray, FetcherList, FetcherStream, FetcherManual {

    public <T> UpdateResult<T> withUpdateCount(
        Function<BatchStatementWithGeneratedKeys, T> stmt
    ) {
        return withUpdateCount(false, stmt);
    }

    public <T> UpdateResult<T> withUpdateCount(
        boolean isLarge, Function<BatchStatementWithGeneratedKeys, T> stmt
    ) {
        return null;
    }

    @Override public PreparedStatement prepare(Connection connection) {
        return null;
    }
}