package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherManual;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.prepare.Transform;
import com.truej.sql.v3.source.RuntimeConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class FetcherManualTest {
    static class Fail extends RuntimeException { }

    @Test void fetchManual() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt(ds, "select id from t1");

            // ok query, no acquire, ok execution
            var r1 = FetcherManual.fetch(Transform.value(), query, new ManagedAction<>() {
                @Override public boolean willStatementBeMoved() {
                    return false;
                }
                @Override public List<Long> apply(
                    RuntimeConfig conf, Void executionResult, PreparedStatement stmt, boolean hasGeneratedKeys
                ) throws SQLException {
                    return List.of(1L, 2L);
//                    // FIXME: remove
//                    return new FetcherList<>(Fixture.longMapper()).apply(conf, null, stmt, false);
                }
            });

            Assertions.assertEquals(r1, List.of(1L, 2L));

            // ok query, no acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () -> FetcherManual.fetch(Transform.value(), query, new ManagedAction<>() {
                        @Override public boolean willStatementBeMoved() {
                            return false;
                        }
                        @Override public Void apply(
                            RuntimeConfig conf, Void executionResult, PreparedStatement stmt, boolean hasGeneratedKeys
                        ) {
                            throw new Fail();
                        }
                    })
            );

            // ok query, do acquire, ok execution
            try (
                var r2 = FetcherManual.fetch(Transform.value(), query, new ManagedAction<PreparedStatement, Void, PreparedStatement>() {
                        @Override public boolean willStatementBeMoved() {
                            return true;
                        }
                        @Override public PreparedStatement apply(
                            RuntimeConfig conf, Void executionResult, PreparedStatement stmt, boolean hasGeneratedKeys
                        ) throws SQLException {
                            return stmt;
//                            // FIXME: remove
//                            return FetcherStream.apply(
//                                conf, stmt, stmt.getResultSet(), Fixture.longMapper()
//                            );
                        }
                    }
                )
            ) {
                Assertions.assertFalse(r2.isClosed());
            }


            // ok query, do acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () -> FetcherManual.fetch(Transform.value(), query, new ManagedAction<PreparedStatement, Void, Long>() {
                        @Override public boolean willStatementBeMoved() {
                            return true;
                        }
                        @Override public Long apply(
                            RuntimeConfig conf, Void executionResult, PreparedStatement stmt, boolean hasGeneratedKeys
                        ) {
                            throw new Fail();
                        }
                    }
                )
            );

            // bad query
            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    FetcherManual.fetch(Transform.value(), Fixture.badQuery(ds), new ManagedAction<PreparedStatement, Void, Long>() {
                            @Override public boolean willStatementBeMoved() {
                                throw new IllegalStateException("not excepted to call");
                            }
                            @Override
                            public Long apply(RuntimeConfig conf, Void executionResult, PreparedStatement stmt, boolean hasGeneratedKeys) {
                                throw new IllegalStateException("not excepted to call");
                            }
                        }
                    )
            );
        });
    }
}
