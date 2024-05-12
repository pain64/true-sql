package com.truej.sql.showcase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __10__InTransaction {
    static class ForceRollback extends Exception { }

    @Test void onDataSource(MainDataSource ds) {
        try {
            ds.inTransaction(cn -> {
                cn."""
                    insert into users values (1, 'Joe', 'example@email.com')
                    """.fetchNone();

                assertEquals(
                    cn."select name from users where id = 1"
                        .fetchOne(String.class)
                    , "Joe"
                );

                throw new ForceRollback();
            });
        } catch (ForceRollback ex) {
            assertNull(
                ds."select name from users where id = 1"
                    .fetchOneOrNull(String.class)
            );
        }
    }

    @Test void onConnection(MainDataSource ds) {
        try {
            ds.withConnection(cn ->
                cn.inTransaction(() -> {
                    cn."""
                            insert into users values (1, 'Joe', 'example@email.com')
                        """.fetchNone();

                    assertEquals(
                        cn."select name from users where id = 1"
                            .fetchOne(String.class)
                        , "Joe"
                    );
                    throw new ForceRollback();
                })
            );
        } catch (ForceRollback ex) {
            assertNull(
                ds."select name from users where id = 1"
                    .fetchOneOrNull(String.class)
            );
        }
    }
}
