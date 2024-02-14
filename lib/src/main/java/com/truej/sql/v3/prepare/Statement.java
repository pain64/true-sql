package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class Statement implements
    ToPreparedStatement,
    FetcherNone, FetcherOne, FetcherOneOrNull, FetcherOneOptional,
    FetcherArray, FetcherList, FetcherStream, FetcherManual {

    public StatementWithGeneratedKeys withGeneratedKeys() {
        return null;
    }

    // withGeneratedKeys()
    // withAffectedRows()

    public StatementWithGeneratedKeys withGeneratedKeys(int[] columnIndexes) {
        return null;
    }

    public StatementWithGeneratedKeys withGeneratedKeys(String[] columnNames) {
        return null;
    }

    public interface StatementBuilder<T, E extends Exception> {
        T buildTo() throws E;
    }

    public <T, E extends Exception> T with(StatementBuilder<T, E> builder) throws E {
        return null;
    }

    @Override
    public PreparedStatement prepare(Connection connection) {
        return null;
    }

    public static class StatementWithGeneratedKeys {}
    public static class StatementWithRowsAffected {}
    public static class StatementWithGeneratedKeysAndRowsAffected {}
}
