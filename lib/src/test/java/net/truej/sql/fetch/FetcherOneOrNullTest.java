package com.truej.sql.fetch;

// import com.truej.sql.v3.prepare.Transform;


public class FetcherOneOrNullTest {
//    @Test void fetchOneOrNull() throws SQLException {
//        Fixture.withDataSource(ds -> {
//
//            Assertions.assertEquals(
//                FetcherOneOrNull.fetch(
//                    Transform.value(),
//                    Fixture.queryStmt(ds, "select id from t1 where id = 1"),
//                    Fixture.longMapper()
//                ),
//                1L
//            );
//
//            Assertions.assertNull(
//                FetcherOneOrNull.fetch(
//                    Transform.value(),
//                    Fixture.queryStmt(ds, "select id from t1 where id = 777"),
//                    Fixture.longMapper()
//                )
//            );
//
//            Assertions.assertThrows(
//                TooMuchRowsException.class, () ->
//                    FetcherOneOrNull.fetch(
//                        Transform.value(),
//                        Fixture.queryStmt(ds, "select id from t1"),
//                        Fixture.longMapper()
//                    )
//            );
//
//            Assertions.assertThrows(
//                SqlExceptionR.class, () ->
//                    FetcherOneOrNull.fetch(
//                        Transform.value(),
//                        Fixture.badQuery(ds),
//                        Fixture.longMapper()
//                    )
//            );
//
//            Assertions.assertThrows(
//                SqlExceptionR.class, () ->
//                    FetcherOneOrNull.fetch(
//                        Transform.value(),
//                        Fixture.queryStmt(ds, "select id from t1 where id = 1"),
//                        Fixture.badMapper()
//                    )
//            );
//        });
//    }
}
