package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.PreparedStatement;

public final class Statement implements ToPreparedStatement,
    FetcherNone.Default, FetcherOne.Default,
    FetcherOneOptional.Default, FetcherOneOrNull.Default,
    FetcherArray.Default, FetcherList.Default,
    FetcherStream.Default, FetcherManual.Default {

    public StatementWithGeneratedKeys withGeneratedKeys() {
        return null;
    }

    public StatementWithGeneratedKeys withGeneratedKeys(int[] columnIndexes) {
        return null;
    }

    public StatementWithGeneratedKeys withGeneratedKeys(String[] columnNames) {
        return null;
    }

    public StatementWithUpdateCount withUpdateCount() {
        return withUpdateCount(false);
    }

    public StatementWithUpdateCount withUpdateCount(boolean isLarge) {
        return null;
    }

    public interface StatementBuilder<T, E extends Exception> {
        T buildTo() throws E;
    }

    public <T, E extends Exception> T with(StatementBuilder<T, E> builder) throws E {
        return null;
    }

    @Override public PreparedStatement prepare() {
        return null;
    }
}
