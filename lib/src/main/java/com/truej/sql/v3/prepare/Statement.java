package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Function;

public final class Statement implements
    ToPreparedStatement,
    FetcherNone, FetcherOne, FetcherOneOrNull, FetcherOneOptional,
    FetcherArray, FetcherList, FetcherStream, FetcherManual {

    public StatementWithGeneratedKeys withGeneratedKeys() {
        return null;
    }

    public StatementWithGeneratedKeys withGeneratedKeys(int[] columnIndexes) {
        return null;
    }

    public StatementWithGeneratedKeys withGeneratedKeys(String[] columnNames) {
        return null;
    }

    public <T> UpdateResult<T> withUpdateCount(
        Function<StatementWithUpdateCount, T> stmt
    ) {
        return withUpdateCount(false, stmt);
    }

    public <T> UpdateResult<T> withUpdateCount(
        boolean isLarge, Function<StatementWithUpdateCount, T> stmt
    ) {
        return null;
    }

    public interface StatementBuilder<T, E extends Exception> {
        T buildTo() throws E;
    }

    public <T, E extends Exception> T with(StatementBuilder<T, E> builder) throws E {
        return null;
    }

    @Override public PreparedStatement prepare(Connection connection) {
        return null;
    }
}
