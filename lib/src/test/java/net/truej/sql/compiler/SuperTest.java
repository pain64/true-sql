package net.truej.sql.compiler;

import net.truej.sql.TrueSql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.source.Parameters.*;


// FIXME: does not works if this record will be in class or method

@ExtendWith(TrueSqlTests.class)
@Disabled
@TrueSql public class SuperTest {
    record T1(long id, String v) { }
    record User(Long id, String name) {}

    // FIXME: make int & Integer compatible (if not null)
    // FIXME: prefer int over Integer in g (if not null)
    record IntPair(Integer y, Integer z) { }

    @Test public void test1(MainConnection cn) {
// TODO: Report the bug to HSQLDB
//        Assertions.assertEquals(
//            100L,
//            cn.q("insert into t1 values(100, ?)", "haha")
//                .asGeneratedKeys("id").fetchOne(Long.class)
//        );
//
//        Assertions.assertEquals(
//            "a",
//            cn.q("select v from t1 where id = ?", 1L)
//                .fetchOne(Nullable, String.class)
//        );
//
//        var x = 1L;
//        Assertions.assertEquals(
//            "a",
//            cn.q("select v from t1 where id = ?", x)
//                .fetchOne(Nullable, String.class)
//        );
//
//        Assertions.assertArrayEquals(
//            new long[]{1L, 1L},
//            cn.q(
//                List.of(new T1(1000L, "x"), new T1(1001L, "y")),
//                "insert into t1 values(?, ?)",
//                v -> new Object[]{v.id, v.v}
//            ).withUpdateCount.fetchNone()
//        );
//
//        var idList = List.of(1L, 2L, 3L);
//
//        Assertions.assertEquals(
//            List.of("a", "b"),
//            cn.q("select v from t1 where id in (?)", unfold(idList))
//                .fetchList(Nullable, String.class)
//        );
//
//// TODO: does not worsk on HSQLDB
////        var pairs = List.of(
////            new Pair<>(1L, "a"), new Pair<>(2L, "b")
////        );
////
////        Assertions.assertEquals(
////            List.of("aa", "bb"),
////            cn.q("select v || v from t1 where (id, v) in (?)", unfold2(pairs))
////                .fetchList(String.class)
////        );
//
//
//        Assertions.assertEquals(
//            new IntPair(84, 126),
//            cn.q("{call p1(?, ?, ?)}", 42, inout(42), out(Integer.class))
//                .asCall().fetchOne(IntPair.class)
//        );
    }
}
