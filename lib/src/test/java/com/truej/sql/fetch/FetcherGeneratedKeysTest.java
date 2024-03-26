package com.truej.sql.fetch;

import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.Concrete;
import com.truej.sql.v3.fetch.FetcherGeneratedKeys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FetcherGeneratedKeysTest {
    // TODO:
    //  0. updateCount и batch: long -> long[]
    //  1. Покрытие тестами всех фетчеров
    //  2. Генерализовать код еще - убрать дублирование логики moved
    //  3. Обработать -1 в getUpdateCount - бросать исключение X
    static class Fail extends RuntimeException { }

    @Test void fetchGeneratedKeys() throws SQLException {
        Fixture.withConnection(connection -> {
            var query = Fixture.queryStmt("insert into t1 values(42, 'v')")
                .withGeneratedKeys("id");

            // ok query, no acquire, ok execution
            Assertions.assertEquals(
                query.fetchGeneratedKeys(
                    connection, new FetcherGeneratedKeys.Next<>() {
                        @Override public boolean willPreparedStatementBeMoved() {
                            return false;
                        }
                        @Override public Long apply(Concrete source) {
                            return 42L;
                        }
                    }
                ), (Long) 42L
            );

            // ok query, no acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () ->
                    query.fetchGeneratedKeys(
                        connection, new FetcherGeneratedKeys.Next<Void>() {
                            @Override public boolean willPreparedStatementBeMoved() {
                                return false;
                            }
                            @Override public Void apply(Concrete source) {
                                throw new Fail();
                            }
                        }
                    )
            );

            // ok query, do acquire, ok execution
            try (
                var moved = query.fetchGeneratedKeys(
                    connection, new FetcherGeneratedKeys.Next<PreparedStatement>() {
                        @Override public boolean willPreparedStatementBeMoved() {
                            return true;
                        }
                        @Override public PreparedStatement apply(Concrete source) {
                            return source.stmt;
                        }
                    }
                )
            ) {
                Assertions.assertFalse(moved.isClosed());
            }

            // ok query, do acquire, bad execution
            Assertions.assertThrows(
                Fail.class, () -> query.fetchGeneratedKeys(
                    connection, new FetcherGeneratedKeys.Next<>() {
                        @Override public boolean willPreparedStatementBeMoved() {
                            return true;
                        }
                        @Override public Void apply(Concrete source) {
                            throw new Fail();
                        }
                    }
                )
            );

            // bad query
            Assertions.assertThrows(
                SqlExceptionR.class, () ->
                    Fixture.BAD_QUERY.fetchGeneratedKeys(
                        connection, new FetcherGeneratedKeys.Next<>() {
                            @Override public boolean willPreparedStatementBeMoved() {
                                throw new IllegalStateException("not excepted to call");
                            }
                            @Override public Long apply(Concrete source) {
                                throw new IllegalStateException("not excepted to call");
                            }
                        }
                    )
            );
        });
    }
}
