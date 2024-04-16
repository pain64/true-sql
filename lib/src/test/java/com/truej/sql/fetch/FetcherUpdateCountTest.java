package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.prepare.BatchStatement;
import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherUpdateCountTest {
    static class Fail extends RuntimeException { }

    @Test void fetchUpdateCount() throws SQLException {
        Fixture.withDataSource(ds -> {
            var query = Fixture.queryStmt("update t1 set v = 'xxx'");

            // ok query, no acquire, ok execution
            var r1 = query.fetchUpdateCount(ds, new ManagedAction.Simple<>() {
                @Override public boolean willStatementBeMoved() {
                    return false;
                }
                @Override public Long apply(RuntimeConfig conf, PreparedStatement stmt) {
                    return 42L;
                }
            });

            Assertions.assertEquals(r1.updateCount, 2L);
            Assertions.assertEquals(r1.value, 42L);

            // ok query, no acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () ->
                    query.fetchUpdateCount(ds, new ManagedAction.Simple<>() {
                        @Override public boolean willStatementBeMoved() {
                            return false;
                        }
                        @Override public Long apply(RuntimeConfig conf, PreparedStatement stmt) {
                            throw new Fail();
                        }
                    })
            );

            // ok query, do acquire, ok execution
            var r2 = query.fetchUpdateCount(
                ds, new ManagedAction.Simple<PreparedStatement, PreparedStatement>() {
                    @Override public boolean willStatementBeMoved() {
                        return true;
                    }
                    @Override
                    public PreparedStatement apply(RuntimeConfig conf, PreparedStatement stmt) {
                        return stmt;
                    }
                }
            );

            Assertions.assertEquals(r2.updateCount, 2L);
            Assertions.assertFalse(r2.value.isClosed());
            r2.value.close();

            // ok query, do acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () ->
                    query.fetchUpdateCount(
                        ds, new ManagedAction.Simple<PreparedStatement, Long>() {
                            @Override public boolean willStatementBeMoved() {
                                return true;
                            }
                            @Override
                            public Long apply(RuntimeConfig conf, PreparedStatement stmt) {
                                throw new Fail();
                            }
                        }
                    )
            );

            // bad query
            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchUpdateCount(
                        ds, new ManagedAction.Simple<PreparedStatement, Long>() {
                            @Override public boolean willStatementBeMoved() {
                                throw new IllegalStateException("not excepted to call");
                            }
                            @Override
                            public Long apply(RuntimeConfig conf, PreparedStatement stmt) {
                                throw new IllegalStateException("not excepted to call");
                            }
                        }
                    )
            );

            // overload
            Assertions.assertEquals(query.fetchUpdateCount(ds), 2);
        });
    }

    @Test void closeThrowsException() throws SQLException {
        Fixture.withDataSource(
            new Fixture.Options(false, true), ds -> {
                var query = Fixture.queryStmt("update t1 set v = 'xxx'");

                var ex = Assertions.assertThrows(
                    Fail.class, () ->
                        query.fetchUpdateCount(
                            ds, new ManagedAction.Simple<PreparedStatement, Long>() {
                                @Override public boolean willStatementBeMoved() {
                                    return false;
                                }
                                @Override
                                public Long apply(RuntimeConfig conf, PreparedStatement stmt) {
                                    throw new Fail();
                                }
                            })
                );

                Assertions.assertEquals(ex.getSuppressed().length, 1);
                Assertions.assertInstanceOf(SQLException.class, ex.getSuppressed()[0]);
            });
    }

    @Test void updateCountOnBatch() throws SQLException {
        Fixture.withDataSource(ds ->
            Assertions.assertArrayEquals(
                new BatchStatement() {
                    @Override protected String query() {
                        return "update t1 set v = 'x' where id = ?";
                    }
                    @Override
                    protected void bindArgs(PreparedStatement stmt) throws SQLException {
                        stmt.setLong(1, 1);
                        stmt.addBatch();
                        stmt.setLong(1, 2);
                        stmt.addBatch();
                    }
                }.fetchUpdateCount(ds),
                new long[]{1L, 1L}
            )
        );
    }
}
