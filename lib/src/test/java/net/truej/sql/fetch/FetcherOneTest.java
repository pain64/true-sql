package com.truej.sql.fetch;

//import com.truej.sql.v3.prepare.Runtime;
//import com.truej.sql.v3.prepare.Transform;


public class FetcherOneTest {
//    @Test void fetchOne() throws SQLException {
//        Fixture.withDataSource(ds -> {
//
//            Assertions.assertEquals(
//                FetcherOne.fetch(
//                    Transform.value(),
//                    Fixture.queryStmt(ds, "select id from t1 where id = 1"),
//                    Fixture.longMapper()
//                ),
//                1L
//            );
//
//            Assertions.assertThrows(
//                TooFewRowsException.class, () ->
//                    FetcherOne.fetch(
//                        Transform.value(),
//                        Fixture.queryStmt(ds, "select id from t1 where id = 777"),
//                        Fixture.longMapper()
//                    )
//            );
//
//            Assertions.assertThrows(
//                TooMuchRowsException.class, () ->
//                    FetcherOne.fetch(
//                        Transform.value(),
//                        Fixture.queryStmt(ds, "select id from t1"),
//                        Fixture.longMapper()
//                    )
//            );
//
//            Assertions.assertThrows(
//                SqlExceptionR.class, () ->
//                    FetcherOne.fetch(Transform.value(), Fixture.badQuery(ds), Fixture.longMapper())
//            );
//
//            Assertions.assertThrows(
//                SqlExceptionR.class, () ->
//                    FetcherOne.fetch(
//                        Transform.value(),
//                        Fixture.queryStmt(ds, "select id from t1 where id = 1"),
//                        Fixture.badMapper()
//                    )
//            );
//        });
//    }
}
