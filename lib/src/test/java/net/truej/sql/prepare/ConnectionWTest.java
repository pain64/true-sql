package com.truej.sql.v3.prepare;

public class ConnectionWTest {
    static class Fail extends Exception { }

    // 0. то что работает commit
    // 1. то что работает rollback
    // 2. то что auto commit возвращается на место

//    @Test void inTransactionOk() throws SQLException {
//        Fixture.withDataSource(ds -> {
//
//            ds.withConnection(cn -> {
//                var before = cn.w().getAutoCommit();
//                Assertions.assertTrue(before);
//
//                var v = cn.inTransaction(() ->
//                    FetcherNone.fetch(Transform.value(), Fixture.queryStmt(cn, "insert into t1 values(100, 'xxy')"))
//                );
//
//                Assertions.assertTrue(cn.w().getAutoCommit());
//                return v;
//            });
//
//            var result = FetcherOne.fetch(
//                Transform.value(),
//                Fixture.queryStmt(ds, "select id from t1 where id = 100"),
//                Fixture.longMapper()
//            );
//
//            Assertions.assertEquals(100L, result);
//        });
//    }
//
//    @Test void inTransactionRollback() throws SQLException {
//        Fixture.withDataSource(ds -> {
//
//            ds.withConnection(cn -> {
//                var before = cn.w().getAutoCommit();
//                Assertions.assertTrue(before);
//
//                Assertions.assertThrows(
//                    Fail.class, () -> cn.inTransaction(() -> {
//                        FetcherNone.fetch(Transform.value(), Fixture.queryStmt(cn, "insert into t1 values(100, 'xxy')"));
//                        throw new Fail();
//                    })
//                );
//
//                Assertions.assertTrue(cn.w().getAutoCommit());
//                return null;
//            });
//
//            var result = FetcherOneOrNull.fetch(
//                Transform.value(),
//                Fixture.queryStmt(ds, "select id from t1 where id = 100"),
//                Fixture.longMapper()
//            );
//
//            Assertions.assertNull(result);
//        });
//    }
//
//    @Test void inTransactionBadRollback() throws SQLException {
//        Fixture.withDataSource(ds ->
//            ds.withConnection(cn -> {
//                Assertions.assertThrows(
//                    SqlExceptionR.class, () ->
//                        cn.inTransaction(() -> {
//                            cn.w().close(); // авария на стройке
//                            return null;
//                        })
//                );
//                return null;
//            })
//        );
//    }
}
