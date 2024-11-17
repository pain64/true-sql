package com.truej.sql.fetch;

// import com.truej.sql.v3.prepare.Transform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.List;

public class ComposeTest {

//    @Test void updateCountAnd() throws Exception {
//        Fixture.withDataSource(ds -> {
//            var query = Fixture
//                .queryStmtGeneratedKeys(ds, "insert into t1 values(42, 'x')", "id");
//                // FIXME
//                // .asGeneratedKeys("id");
//
//            try (
//                var result = FetcherStream.fetch(
//                    Transform.updateCountAndValue(), query, Fixture.longMapper()
//                ).autoClosable()
//            ) {
//                Assertions.assertEquals(result.updateCount, 1L);
//                Assertions.assertEquals(result.value.toList(), List.of(42L));
//            }
//
//        });
//
//        Fixture.withDataSource(ds -> {
//            var query = Fixture
//                .queryStmtGeneratedKeys(ds, "insert into t1 values(42, 'x')", "id");
//                // FIXME
//                //.asGeneratedKeys("id");
//
//            var result2 = FetcherList.fetch(Transform.updateCountAndValue(), query, Fixture.longMapper(), 0);
//
//            Assertions.assertEquals(result2.updateCount, 1L);
//            Assertions.assertEquals(result2.value, List.of(42L));
//        });
//    }
//
//    <T> T map(ResultSet rs, Class<T> toType) {
//        if(toType == String.class)
//            return (T) "42";
//
//        throw new RuntimeException("unreachable");
//    }

//    @Test void generatedKeysAndList() throws SQLException {
//        Fixture.withDataSource(ds -> {
//            var query = Fixture.queryStmt(ds, "insert into t1 values(42, 'v')")
//                .asGeneratedKeys("id");
//            Assertions.assertEquals(
//                42L, query.fetchOne(Fixture.longMapper())
//            );
//        });
//
//        Fixture.withDataSource(ds -> {
//            var query = Fixture.queryStmt(ds, "insert into t1 values(42, 'v')")
//                .asGeneratedKeys("id");
//            Assertions.assertEquals(
//                42L, query.fetchOne(Fixture.longMapper())
//            );
//        });
//    }
}
