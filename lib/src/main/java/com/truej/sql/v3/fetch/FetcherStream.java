package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.Base;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.prepare.Runtime;
import com.truej.sql.v3.prepare.Transform;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.truej.sql.v3.prepare.Runtime.managed;

public final class FetcherStream {
    public static <P extends PreparedStatement, R, U, T, V> V fetch(
        Transform<P, R, U, Stream<T>, V> t, Base<?, P, R, U> base, ResultSetMapper<T> mapper
    ) {
        return managed(base, new ManagedAction<>() {
            @Override public boolean willStatementBeMoved() { return true; }
            @Override public V apply(
                RuntimeConfig conf, R executionResult,
                P stmt, boolean hasGeneratedKeys
            ) throws SQLException {
                var iterator = mapper.map(
                    Runtime.getResultSet(stmt, hasGeneratedKeys)
                );

                return t.transform(
                    base, executionResult, stmt, StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false
                    ).onClose(() -> {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            throw conf.mapException(e);
                        }
                    })
                );
            }
        });
    }
}
