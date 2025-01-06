package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.MainDataSourceUnchecked;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@TrueSql public class __15__AnotherLibraryCallRecognizedAsTrueSql {
    static class Confusion {
        Confusion q(String text) { return this; }
        Confusion q(Integer t1, String t2, String t3, String t4) { return this; }
        Confusion bar() { return this; }
        Void fetchNone() { return null; }
        public void constraint(String sn, String tn) { }
        public void constraint(String sn, String tn, Integer cn, String none) { }
        public void constraint(String sn, String tn, String cn, String none) { }
        public void constraint(String sn, String tn, String cn, Integer some, String none) { }
        public void constraint(String sn, String tn, String cn, Integer some, String none, String ss) { }
        void fetchOne(String one, String two) {};
        void fetchOne(String one, String two, String three) {};
    }

    // TrueSql recognizes that it's not genuine TrueSql api call!
    // Errors discovered for this tree in annotation processor
    // will be discarded in compiler plugin
    @TestTemplate void test() {
        var cn = new Confusion();
        cn.q("").fetchNone();
    }

    @TestTemplate public void test1() {
        var con = new Confusion();
        con.constraint("a", "a", 1, null);
    }

    @TestTemplate public void test2() {
        var con = new Confusion();
        con.constraint("a", "a", "a", 1, null);
    }

    @TestTemplate public void test4() {
        var con = new Confusion();
        con.fetchOne("a", "b", "c");
    }

    @TestTemplate public void test5() {
        var con = new Confusion();
        con.q(1, "1", "1", "1").fetchNone();
    }

    @TestTemplate public void test6() {
        var con = new Confusion();
        con.q(1, "1", "1", "1").fetchOne("1", "1");
    }

    @TestTemplate public void test7() {
        var con = new Confusion();
        con.bar().fetchNone();
    }

    @TestTemplate public void test8() {
        var con = new Confusion();
        con.constraint("1", "2");
    }

    @TestTemplate public void test9() {
        var con = new Confusion();
        con.constraint("1", "2", "3", 1, "5", "6");
    }

    @TestTemplate public void test10() {
        var con = new Confusion();
        var cn = con;
        cn.constraint("1", "2", 4, "3");
    }

    @TestTemplate public void test11() {
        var con = new Confusion();
        var cn = con;
        cn.constraint("1", "2", "1", "3");
    }
}
