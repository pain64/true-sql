package net.truej.sql.test.negative.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __15__AnotherLibraryCallRecognizedAsTrueSql {
    static class Confusion {
        Confusion g = this;
        Confusion q(String text) { return this; }
        Confusion q(Integer t1, String t2, String t3, String t4) { return this; }
        Confusion bar() { return this; }
        Void fetchNone() { return null; }
        Void fetchNone(int x) { return null; }
        Confusion asGeneratedKeys() { return this; }
        Confusion asGeneratedKeys(float f1) { return this; }

        public void constraint(String tn, String сn, Runnable r) { }
        public void constraint(String sn, String cn, String tn, Runnable r) { }
        public void constraint(String catn, String sn, String cn, String tn, Runnable r) { }

        public void constraint(int x1, int x2) { }
        public void constraint(int x1, int x2, int x3, int x4, int x5, int x6) { }

        void fetchOne(String one, String two) {};
        void fetchOne(String one, String two, String three) {};
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

    @TestTemplate public void testAsGeneratedKeys() {
        var con = new Confusion();
        con.asGeneratedKeys().fetchNone();
        con.asGeneratedKeys(42f).fetchNone();
    }

    @TestTemplate public void testGMissedArgument() {
        var con = new Confusion();
        con.q("").g.fetchNone();
    }

    @TestTemplate public void testFetchNoneWithArgs() {
        var con = new Confusion();
        con.q("").fetchNone(1);
    }

    @TestTemplate public void testTooLittleParametersCount() {
        var con = new Confusion();
        con.constraint(1, 2);
    }

    @TestTemplate public void testTooMuchParametersCount() {
        var con = new Confusion();
        con.constraint(1, 2, 3, 4, 5, 6);
    }

    @TestTemplate public void testBadSourceUnknownVarType() {
        var con = new Confusion();
        var cn = con;
        cn.constraint("", "", "", "", () -> {});
    }

    // TrueSql recognizes that it's not genuine TrueSql api call!
    // Errors discovered for this tree in annotation processor
    // will be discarded in compiler plugin

    @TestTemplate public void testBadSourceBadVarType() {
        var con = new Confusion();
        con.constraint("", "", "", "", () -> {});
    }

    @TestTemplate public void testBadLiteral() {
        var con = new Confusion();
        con.constraint(null, "a", "a", "", () -> {});
        con.constraint(null, "a", "", () -> {});
    }
}
