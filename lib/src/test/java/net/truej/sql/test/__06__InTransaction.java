package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __06__InTransaction {
    static class ForceRollback extends Exception { }

    @TestTemplate public void testConnection(MainConnection cn) {
        try {
            cn.inTransaction(() -> {
                cn.q("insert into clinic values(?, ?, ?)", 4, "Paris St. Marie Hospital", 2)
                    .fetchNone();

                assertEquals(
                    cn.q("select name from clinic where id = ?", 4)
                        .fetchOne(String.class)
                    , "Paris St. Marie Hospital"
                );
                throw new ForceRollback();
            });
        } catch (ForceRollback ex) {
            assertNull(
                cn.q("select name from clinic where id = ?", 4)
                    .fetchOneOrZero(String.class)
            );
        }
    }

    @TestTemplate public void testDataSource(MainDataSource ds) {
        try {
            ds.withConnection(cn ->
                cn.inTransaction(() -> {
                    cn.q("insert into clinic values(?, ?, ?)", 4, "Paris St. Marie Hospital", 2)
                        .fetchNone();

                    assertEquals(
                        cn.q("select name from clinic where id = ?", 4)
                            .fetchOne(String.class)
                        , "Paris St. Marie Hospital"
                    );
                    throw new ForceRollback();
                })
            );
        } catch (ForceRollback ex) {
            assertNull(
                ds.q("select name from clinic where id = ?", 4)
                    .fetchOneOrZero(String.class)
            );
        }
    }
}
