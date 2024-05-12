package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.Base;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.prepare.Transform;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.truej.sql.v3.prepare.Runtime.managed;

public final class FetcherNone {
    public static <P extends PreparedStatement, R, U, V> V fetch(
        Transform<P, R, U, Void, V> t, Base<?, P, R, U> base
    ) {
        return managed(base, new ManagedAction<>() {
            @Override public boolean willStatementBeMoved() { return false; }
            @Override public V apply(
                RuntimeConfig conf, R executionResult,
                P stmt, boolean hasGeneratedKeys
            ) throws SQLException {
                return t.transform(base, executionResult, stmt, null);
            }
        });
    }
}
