package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.Base;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.prepare.Transform;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.truej.sql.v3.prepare.Runtime.managed;

public final class FetcherList {

    public static <P extends PreparedStatement, R, U, T, V> V fetch(
        Transform<P, R, U, List<T>, V> t, Base<?, P, R, U> base,
        ResultSetMapper<T> mapper, int expectedSize
    ) {
        // TODO: move transform as arg ???
        return managed(base, new ManagedAction<>() {
            @Override public boolean willStatementBeMoved() { return false; }
            @Override public V apply(
                RuntimeConfig conf, R executionResult,
                P stmt, boolean hasGeneratedKeys
            ) throws SQLException {
                var iterator = mapper.map(
                    Runtime.getResultSet(stmt, hasGeneratedKeys)
                );

                var result = expectedSize != 0
                    ? new ArrayList<T>(expectedSize)
                    : new ArrayList<T>();

                while (iterator.hasNext())
                    result.add(iterator.next());

                return t.transform(base, executionResult, stmt, result);
            }
        });
    }
}
