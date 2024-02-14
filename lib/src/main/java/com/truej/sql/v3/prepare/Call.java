package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.FetcherNone;
import com.truej.sql.v3.fetch.FetcherOne;
import com.truej.sql.v3.fetch.ToPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class Call implements
    ToPreparedStatement, FetcherNone, FetcherOne {
    public static class Parameters {
        public static <T> T out(T value, String parameterName) {
            return value;
        }

        public static <T> T inout(T value, String parameterName) {
            return value;
        }
    }

    @Override public PreparedStatement prepare(Connection connection) {
        return null;
    }

    public static class CallWithAffectedRows {}
}
