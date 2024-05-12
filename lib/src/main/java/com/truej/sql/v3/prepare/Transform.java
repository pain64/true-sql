package com.truej.sql.v3.prepare;

import com.truej.sql.v3.fetch.UpdateResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Transform<P extends PreparedStatement, R, U, T, V> {
    V transform(Base<?, P, R, U> base, R executionResult, P stmt, T value) throws SQLException;

    static <P extends PreparedStatement, R, U, T> Transform<P, R, U, T, T> value() {
        return (_, _, _, v) -> v;
    }

    static <P extends PreparedStatement, R, U, T>
    Transform<P, R, U, T, UpdateResult<U, T>> updateCountAndValue() {
        return (base, executionResult, stmt, v) -> new UpdateResult<>(
            base.getUpdateCount(executionResult, stmt), v
        );
    }

    static <P extends PreparedStatement, R, U, T>
    Transform<P, R, U, T, U> updateCount() {
        return (base, executionResult, stmt, v) ->
            base.getUpdateCount(executionResult, stmt);
    }
}
