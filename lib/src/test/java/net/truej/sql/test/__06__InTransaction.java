package net.truej.sql.test;

import net.truej.sql.fetch.SqlExceptionR;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __06__InTransaction {
    static class ForceRollback extends Exception { }

    @TestTemplate public void testConnectionThrows(MainConnection cn) {
        try {
            cn.inTransaction(() -> {
                cn.q("insert into clinic values(?, ?, ?)", 4L, "Paris St. Marie Hospital", 2L)
                    .fetchNone();

                assertEquals(
                    cn.q("select name from clinic where id = ?", 4L)
                        .fetchOne(String.class)
                    , "Paris St. Marie Hospital"
                );
                throw new ForceRollback();
            });
        } catch (ForceRollback ex) {
            assertNull(
                cn.q("select name from clinic where id = ?", 4L)
                    .fetchOneOrZero(String.class)
            );
        }
    }

    @TestTemplate public void testConnectionOk(MainConnection cn) {

        cn.inTransaction(() ->
            cn.q("insert into clinic values(?, ?, ?)", 4L, "Paris St. Marie Hospital", 2L)
                .fetchNone()
        );

        Assertions.assertEquals(
            "Paris St. Marie Hospital",
            cn.q("select name from clinic where id = ?", 4L).fetchOne(String.class)
        );
    }

    @TestTemplate public void testConnectionThrowsSQLException(MainConnection cn) {
        Assertions.assertThrows(
            SqlExceptionR.class,
            () -> cn.inTransaction(() -> {
                    cn.q("insert into clinic values(?, ?, ?)", 4L, "Paris St. Marie Hospital", 2L)
                        .fetchNone();
                    throw new SQLException("Nice");
                }
            )
        );
    }

    @TestTemplate public void testDataSource(MainDataSource ds) {
        try {
            ds.withConnection(cn ->
                cn.inTransaction(() -> {
                    cn.q("insert into clinic values(?, ?, ?)", 4L, "Paris St. Marie Hospital", 2L)
                        .fetchNone();

                    assertEquals(
                        cn.q("select name from clinic where id = ?", 4L)
                            .fetchOne(String.class)
                        , "Paris St. Marie Hospital"
                    );
                    throw new ForceRollback();
                })
            );
        } catch (ForceRollback ex) {
            assertNull(
                ds.q("select name from clinic where id = ?", 4L)
                    .fetchOneOrZero(String.class)
            );
        }
    }

    @TestTemplate public void testDataSourceInTransaction(MainDataSource ds) {
        ds.inTransaction(cn -> {
            cn.q("insert into clinic values(?, ?, ?)", 4L, "Paris St. Marie Hospital", 2L)
                .fetchNone();

            assertEquals(
                cn.q("select name from clinic where id = ?", 4L)
                    .fetchOne(String.class)
                , "Paris St. Marie Hospital"
            );
            return null;
        });
    }

    @TestTemplate public void testDataSourceThrowsSQLException(MainDataSource ds) {
        Assertions.assertThrows(
            SqlExceptionR.class,
            () -> ds.withConnection( cn -> {
                    cn.q("insert into clinic values(?, ?, ?)", 4L, "Paris St. Marie Hospital", 2L)
                        .fetchNone();
                    throw new SQLException("Nice");
                }
            )
        );
    }

    @TestTemplate public void testDataSourceThrowsSQLException2(MainDataSource ds) {
        Assertions.assertThrows(
            SqlExceptionR.class,
            () -> ds.inTransaction( cn -> {
                    cn.q("insert into clinic values(?, ?, ?)", 4L, "Paris St. Marie Hospital", 2L)
                        .fetchNone();
                    throw new SQLException("Nice");
                }
            )
        );
    }
}
