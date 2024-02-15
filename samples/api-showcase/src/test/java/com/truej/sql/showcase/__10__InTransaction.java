package com.truej.sql.showcase;

import com.truej.sql.v3.TrueJdbc;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static com.truej.sql.v3.TrueJdbc.Stmt;
import static com.truej.sql.v3.TrueJdbc.m;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __10__InTransaction {
    class ForceRollback extends Exception { }

    @Test void simple(DataSource ds) {
        try {
            TrueJdbc.inTransaction(ds, cn -> {
                Stmt."""
                        insert into users values (1, 'Joe', 'example@email.com')
                    """.fetchNone(cn);

                assertEquals(
                    Stmt."select name from users where id = 1"
                        .fetchOne(cn, m(String.class))
                    , "Joe"
                );

                throw new ForceRollback();
            });
        } catch (ForceRollback ex) {
            assertNull(
                Stmt."select name from users where id = 1"
                    .fetchOneOrNull(ds, m(String.class))
            );
        }
    }
}
