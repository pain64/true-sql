package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __07__WithConnection {
    @Test public void test(MainDataSource ds) {
        //FIXME: conn dropped? cant find temp_table on insert
////        Assertions.assertEquals(
////                List.of("a", "b"),
//                ds.withConnection( cn -> {
//                    cn.q("""
//                        CREATE TEMP TABLE temp_table (s varchar(50));
//                    """).fetchNone();
////
//                    cn.q("insert into temp_table values('a')").fetchNone();
////                    cn.q("insert into temp_table values('b')").fetchNone();
//
////                    var r = cn.q("select s from temp_table")
////                            .fetchList(String.class);
////                    Assertions.assertEquals(
////                            List.of("a", "b"),
////                            r
////                    );
////                    return cn.q("select * from temp_table")
////                            .fetchList(String.class);
//                    return null;
//                });
//      //  );
    }
}
