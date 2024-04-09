package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.FetcherGeneratedKeys;
import com.truej.sql.v3.prepare.Statement;
import com.truej.sql.v3.source.RuntimeConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetcherGeneratedKeysTest {
    // TODO:
    //  1. Покрытие тестами всех фетчеров
    static class Fail extends RuntimeException { }

    void testSuite(Statement query) throws SQLException {
        Fixture.withDataSource(ds -> {
            // ok query, no acquire, ok execution
            Assertions.assertEquals(
                query.fetchGeneratedKeys(
                    ds, new FetcherGeneratedKeys.Next<>() {
                        @Override public boolean willStatementBeMoved() {
                            return false;
                        }
                        @Override public Long apply(
                            RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
                        ) {
                            return 42L;
                        }
                    }
                ), (Long) 42L
            );
        });

        Fixture.withDataSource(ds -> {

            // ok query, no acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () ->
                    query.fetchGeneratedKeys(
                        ds, new FetcherGeneratedKeys.Next<Void>() {
                            @Override public boolean willStatementBeMoved() {
                                return false;
                            }
                            @Override public Void apply(
                                RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
                            ) {
                                throw new Fail();
                            }
                        }
                    )
            );
        });

        Fixture.withDataSource(ds -> {
            // ok query, do acquire, ok execution
            try (
                var moved = query.fetchGeneratedKeys(
                    ds, new FetcherGeneratedKeys.Next<PreparedStatement>() {
                        @Override public boolean willStatementBeMoved() {
                            return true;
                        }
                        @Override public PreparedStatement apply(
                            RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
                        ) {
                            return stmt;
                        }
                    }
                )
            ) {
                Assertions.assertFalse(moved.isClosed());
            }
        });

        Fixture.withDataSource(ds -> {
            // ok query, do acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () -> query.fetchGeneratedKeys(
                    ds, new FetcherGeneratedKeys.Next<>() {
                        @Override public boolean willStatementBeMoved() {
                            return true;
                        }
                        @Override public Void apply(
                            RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
                        ) {
                            throw new Fail();
                        }
                    }
                )
            );
        });

        Fixture.withDataSource(ds -> {
            // bad query
            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchGeneratedKeys(
                        ds, new FetcherGeneratedKeys.Next<>() {
                            @Override public boolean willStatementBeMoved() {
                                throw new IllegalStateException("not excepted to call");
                            }
                            @Override public Long apply(
                                RuntimeConfig conf, PreparedStatement stmt, ResultSet rs
                            ) {
                                throw new IllegalStateException("not excepted to call");
                            }
                        }
                    )
            );
        });
    }

    // Statement+, Call-, BatchStatement+, BatchCall-
    @Test void fetchGeneratedKeysStmt() throws SQLException {
        testSuite(
            Fixture.queryStmt("insert into t1 values(42, 'v')")
                .withGeneratedKeys("id")
        );
        testSuite(
            Fixture.queryStmt("insert into t1 values(42, 'v')")
                .withGeneratedKeys(1)
        );
        testSuite(
            Fixture.queryStmt("insert into t1 values(42, 'v')")
                .withGeneratedKeys()
        );
    }
}
