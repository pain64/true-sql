package com.truej.sql.v3.prepare;

import com.truej.sql.fetch.Fixture;
import com.truej.sql.v3.SqlExceptionR;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class ConnectionWTest {
    static class Fail extends Exception { }

    // 0. то что работает commit
    // 1. то что работает rollback
    // 2. то что auto commit возвращается на место

    @Test void inTransactionOk() throws SQLException {
        Fixture.withDataSource(ds -> {

            ds.withConnection(cn -> {
                var before = cn.w().getAutoCommit();
                Assertions.assertTrue(before);

                var v = cn.inTransaction(() ->
                    Fixture.queryStmt("insert into t1 values(100, 'xxy')")
                        .fetchNone(cn)
                );

                Assertions.assertTrue(cn.w().getAutoCommit());
                return v;
            });

            var result = Fixture.queryStmt("select id from t1 where id = 100")
                .fetchOne(ds, Fixture.longMapper(null));

            Assertions.assertEquals(100L, result);
        });
    }

    @Test void inTransactionRollback() throws SQLException {
        Fixture.withDataSource(ds -> {

            ds.withConnection(cn -> {
                var before = cn.w().getAutoCommit();
                Assertions.assertTrue(before);

                Assertions.assertThrows(
                    Fail.class, () -> cn.inTransaction(() -> {
                        Fixture.queryStmt("insert into t1 values(100, 'xxy')")
                            .fetchNone(cn);
                        throw new Fail();
                    })
                );

                Assertions.assertTrue(cn.w().getAutoCommit());
                return null;
            });

            var result = Fixture.queryStmt("select id from t1 where id = 100")
                .fetchOneOrNull(ds, Fixture.longMapper(null));

            Assertions.assertNull(result);
        });
    }

    @Test void inTransactionBadRollback() throws SQLException {
        Fixture.withDataSource(ds ->
            ds.withConnection(cn -> {
                Assertions.assertThrows(
                    SqlExceptionR.class, () ->
                        cn.inTransaction(() -> {
                            cn.w().close(); // авария на стройке
                            return null;
                        })
                );
                return null;
            })
        );
    }
}
