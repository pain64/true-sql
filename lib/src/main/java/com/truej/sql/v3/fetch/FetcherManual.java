package com.truej.sql.v3.fetch;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class FetcherManual {

    // FIXME: PreparedCall
    public interface PreparedStatementExecutor<T, E extends Exception> {
        T execute(PreparedStatement stmt) throws E;
    }

    public static <T, E extends Exception> T fetch(
        Connection cn, PreparedStatementExecutor<T, E> executor
    ) throws E {
        return null;
    }
}
