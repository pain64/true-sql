package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.Base;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.prepare.Transform;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.truej.sql.v3.prepare.Runtime.managed;

public class FetcherManual {
    public static <P extends PreparedStatement, R, U, T, V> V fetch(
        Transform<P, R, U, T, V> t, Base<?, P, R, U> base, ManagedAction<P, R, T> action
    ) {
        return managed(base, new ManagedAction<>() {
            @Override public boolean willStatementBeMoved() { return action.willStatementBeMoved(); }
            @Override public V apply(
                RuntimeConfig conf, R executionResult,
                P stmt, boolean hasGeneratedKeys
            ) throws SQLException {
                var result = action.apply(conf, executionResult, stmt, hasGeneratedKeys);
                return t.transform(base, executionResult, stmt, result);
            }
        });
    }
}
