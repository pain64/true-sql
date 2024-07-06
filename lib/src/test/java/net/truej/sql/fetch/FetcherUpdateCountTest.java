package com.truej.sql.fetch;

//import com.truej.sql.v3.prepare.BatchStatement;
//import com.truej.sql.v3.prepare.Transform;

public class FetcherUpdateCountTest {
    static class Fail extends RuntimeException { }

//    @Test void fetchUpdateCount() throws SQLException {
//        // TODO: test all real scenarios ??? (FetcherList?, FetcherOne?)
//        Fixture.withDataSource(ds -> {
//            var query = Fixture.queryStmt(ds, "update t1 set v = 'xxx'");
//
//            var r1 = FetcherNone.fetch(Transform.updateCountAndValue(), query);
//
//            Assertions.assertEquals(r1.updateCount, 2L);
//            Assertions.assertNull(r1.value);
//        });
//    }
//
//    @Test void updateCountOnBatch() throws SQLException {
//        Fixture.withDataSource(ds ->
//            Assertions.assertArrayEquals(
//                FetcherNone.fetch(
//                    Transform.updateCount(), new BatchStatement<>() {
//                        @Override protected Source source() { return ds; }
//                        @Override protected PreparedStatement prepare(Connection connection) throws SQLException {
//                            return connection.prepareStatement("update t1 set v = 'x' where id = ?");
//                        }
//                        @Override
//                        protected void bindArgs(PreparedStatement stmt) throws SQLException {
//                            stmt.setLong(1, 1);
//                            stmt.addBatch();
//                            stmt.setLong(1, 2);
//                            stmt.addBatch();
//                        }
//                    }),
//                new long[]{1L, 1L}
//            )
//        );
//    }
}
