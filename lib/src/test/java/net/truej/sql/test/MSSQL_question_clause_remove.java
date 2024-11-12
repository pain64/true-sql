package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(MARIADB)
@TrueSql public class MSSQL_question_clause_remove {
    @TestTemplate
    public void test(MainConnection cn) throws SQLException {
        // TODO: в схеме указано numeric(15, 3)
        // но драйвер вместо 100.241 привозит 100.2410
        var connection = cn.w;

        //test1
//        var stmt = connection.createStatement();
//        stmt.execute("create table test (value int not null)"); //for PG: null not null. union return UNKNOWN!!!
//        stmt.execute("insert into test values(1);");
//
//        var pStmt = connection.prepareStatement("select value from test union select value from test");
//        var metaData = pStmt.getMetaData();
//        Assertions.assertEquals(0, metaData.isNullable(1));

//        var stmt = connection.createStatement();
//        stmt.execute("create table test (value int not null)"); //for PG: null not null. union return UNKNOWN!!!
//        stmt.execute("insert into test values(1);");
//
//        var pStmt = connection.prepareStatement("""
//            select 1 as "privet" """);
//        var metaData = pStmt.getMetaData();
//        Assertions.assertEquals(0, metaData.isNullable(1));

        //test2
//        var stmt = connection.createStatement();
//        stmt.execute("create table table1(id1 int not null)");
//        stmt.execute("create table table2(id2 int not null)");
//        stmt.execute("create table table3(id3 int not null)");
//        stmt.execute("insert into table1 values(1)");
//        stmt.execute("insert into table1 values(2)");
//        stmt.execute("insert into table1 values(3)");
//
//        var pStmt = connection.prepareStatement("""
//            select id1, id2
//            from table1 t1
//                left join table2 t2 on t1.id1 = t2.id2
//            """);
//        var metaData = pStmt.getMetaData();
//        Assertions.assertEquals(0, metaData.isNullable(1));
//        Assertions.assertEquals(1, metaData.isNullable(2));

//        System.out.println(metaData.isNullable(1));
//        System.out.println(metaData.isNullable(2));
    //test3
//        stmt.execute("create table table1(id1 int not null)");
//        stmt.execute("create table table2(id2 int not null)");
//        stmt.execute("create table table3(id3 int not null)");
//        stmt.execute("insert into table1 values(1)");
//        stmt.execute("insert into table1 values(2)");
//        stmt.execute("insert into table1 values(3)");
//
//        var pStmt = connection.prepareStatement("""
//            select id1, id2 from table1 t1 inner join table2 t2 on t1.id1 = t2.id2
//            """);
//        var metaData = pStmt.getMetaData();
//        System.out.println(metaData.isNullable(1));
//        System.out.println(metaData.isNullable(2));

//        //test4
//        var stmt = connection.createStatement();
//        stmt.execute("create table table1(idi1 int not null, idf1 int not null)");
//        stmt.execute("create table table2(idi2 int not null, idf2 int not null)");
//        stmt.execute("create table table3(idi3 int not null, idf3 int not null)");
//        stmt.execute("insert into table1 values(1,2)");
//        stmt.execute("insert into table1 values(2,3)");
//        stmt.execute("insert into table1 values(3,4)");
//
//        var pStmt = connection.prepareStatement("""
//            select idi1, idi2, idi3
//            from table1 t1
//                left join table2 t2 on t1.idf1 = t2.idi2
//                inner join table3 t3 on t2.idf2 = t3.idi3
//            """);
//        var metaData = pStmt.getMetaData();
//        Assertions.assertEquals(0, metaData.isNullable(1));
//        Assertions.assertEquals(0, metaData.isNullable(2));
//        Assertions.assertEquals(0, metaData.isNullable(3));
//
//        System.out.println(metaData.isNullable(1));
//        System.out.println(metaData.isNullable(2));
//        System.out.println(metaData.isNullable(3));

        var stmt = connection.createStatement();
        stmt.execute("create table table1(idi1 int not null, idf1 int not null)");
        stmt.execute("create table table2(idi2 int not null, idf2 int not null)");
        stmt.execute("create table table3(idi3 int not null, idf3 int not null)");
        stmt.execute("insert into table1 values(1,2)");
        stmt.execute("insert into table1 values(2,3)");
        stmt.execute("insert into table1 values(3,4)");

        var pStmt = connection.prepareStatement("""
            select avg(idi1) as idi1
            from table1 t1
            """);
        var metaData = pStmt.getMetaData();
        //Assertions.assertEquals(0, metaData.isNullable(1));

        System.out.println(metaData.isNullable(1));
    }
}
