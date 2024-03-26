package com.truej.sql.fetch;

public class ComposeTest {
//    @Test void updateCountAndGeneratedKeys() throws SQLException {
//        Fixture.withConnection(connection -> {
//            var query = Fixture
//                .queryStmt("insert into t1 values(42, 'x')")
//                .withGeneratedKeys("id");
//
//            var result = query.fetchUpdateCount(
//                connection, new FetcherGeneratedKeys<>(
//                    new FetcherOne<>(Fixture.longMapper(null))
//                )
//            );
//
//            Assertions.assertEquals(result.updateCount, 1L);
//            Assertions.assertEquals(result.value, 42L);
//        });
//    }
//
//    @Test void generatedKeysAndArray() throws SQLException {
//        Fixture.withConnection(connection -> {
//            var query = Fixture
//                .queryStmt("update t1 set v = 'x'")
//                .withGeneratedKeys("id");
//
//            Assertions.assertArrayEquals(
//                query.fetchGeneratedKeys(
//                    connection, new FetcherArray<>(Fixture.longMapper(null))
//                ), new Long[]{1L, 2L});
//        });
//    }
}
