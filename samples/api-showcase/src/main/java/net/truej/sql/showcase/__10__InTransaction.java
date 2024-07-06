package net.truej.sql.showcase;

import net.truej.sql.source.ConnectionW;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class __10__InTransaction {
    static class ForceRollback extends Exception { }

    void onDataSource(MainDataSource ds) {
        try {
            ds.inTransaction(cn -> {
//                cn."""
//                    insert into users values (1, 'Joe', 'example@email.com')
//                    """.fetchNone();

                assertEquals(
                    cn.q("select name from users where id = 1")
                        .fetchOne(String.class)
                    , "Joe"
                );

                throw new ForceRollback();
            });
        } catch (ForceRollback ex) {
            assertNull(
                ds.q("select name from users where id = 1")
                    .fetchOneOrZero(String.class)
            );
        }
    }

    void onConnection(MainDataSource ds) {
        try {
            ds.withConnection((ConnectionW cn) ->
                cn.inTransaction(() -> {
                    cn.q("""
                            insert into users values (1, 'Joe', 'example@email.com')
                        """).fetchNone();

                    assertEquals(
                        cn.q("select name from users where id = 1")
                            .fetchOne(String.class)
                        , "Joe"
                    );
                    throw new ForceRollback();
                })
            );
        } catch (ForceRollback ex) {
            assertNull(
                ds.q("select name from users where id = 1")
                    .fetchOneOrZero(String.class)
            );
        }
    }
}
