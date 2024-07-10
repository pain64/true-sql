package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.PgConnection;
import net.truej.sql.compiler.PgDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(TrueSqlTests.class)
@TrueSql
public class __07__WithConnection {

    @Test public void test(MainDataSource ds) {
        //FIXME: conn dropped? cant find temp_table on insert

        ds.withConnection(cn -> {
            //cn.inTransaction(() -> {
//                    cn.q("""
//                        CREATE GLOBAL TEMPORARY TABLE names2(
//                                name varchar(100)
//                        ) ON COMMIT DELETE ROWS;
//                        """).fetchNone();

                    cn.q("""
                                    insert into names2(name) values ('Joe'), ('Donald')
                                """).fetchNone();
                    //System.out.println(s);
                    //cn.q("insert into names2(name) values ('Joe'), ('Donald');").fetchNone();

//                    Assertions.assertEquals(
//                        List.of("Joe", "Donald"),
//                        cn.q("select name from names2;")
//                            .fetchList(String.class)
//                    );
                    //return null;
               // }
            //);
            return null;
        });
    }
//    @Test public void test2(PgConnection cn) {
//        //FIXME: conn dropped? cant find temp_table on insert
//            cn.inTransaction(() -> {
//                    cn.q("""
//                                CREATE TEMPORARY TABLE names(
//                                    name varchar(100)
//                                 );
//                            """).fetchNone();
//
//                    cn.q("insert into names(name) values ('Joe'), ('Donald');").fetchNone();
//
//                    Assertions.assertEquals(
//                        List.of("Joe", "Donald"),
//                        cn.q("select name from names;")
//                            .fetchList(String.class)
//                    );
//                    return null;
//            });
//    }
}
