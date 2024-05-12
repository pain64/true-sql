package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.Base;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.prepare.Transform;
import com.truej.sql.v3.source.RuntimeConfig;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static com.truej.sql.v3.prepare.Runtime.managed;

public final class FetcherOneOptional {
    public static <P extends PreparedStatement, R, U, T, V> V fetch(
        Transform<P, R, U, Optional<T>, V> t, Base<?, P, R, U> base, ResultSetMapper<T> mapper
    ) {
        return managed(base, new ManagedAction<>() {
            @Override public boolean willStatementBeMoved() { return false; }
            @Override public V apply(
                RuntimeConfig conf, R executionResult,
                P stmt, boolean hasGeneratedKeys
            ) throws SQLException {
                return t.transform(
                    base, executionResult, stmt,
                    Optional.ofNullable(FetcherOneOrNull.fetch(stmt, hasGeneratedKeys, mapper))
                );
            }
        });
    }
}
