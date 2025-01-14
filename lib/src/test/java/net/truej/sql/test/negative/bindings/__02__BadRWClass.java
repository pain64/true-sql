package net.truej.sql.test.negative.bindings;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.config.TypeReadWrite;
import net.truej.sql.source.ConnectionW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.*;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.EnableOn;
import static net.truej.sql.compiler.TrueSqlTests.Message;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text = """
        For source net.truej.sql.test.negative.bindings.__02__BadRWClass_.Cn1: \
        RW class net.truej.sql.test.negative.bindings.__02__BadRWClass_$BadRw1: \
        cannot be abstract, if inner then required to be static also, \
        must have public no-arg constructor"""
)
@TrueSql public class __02__BadRWClass {

    @SuppressWarnings("InnerClassMayBeStatic")
    abstract class BadRw1 implements TypeReadWrite<String> {
        public BadRw1(int x) { } // public but not no-arg
        BadRw1() { } // non-public constructor
    }

    @Configuration(
        typeBindings = @TypeBinding(rw = BadRw1.class)
    ) static class Cn1 extends ConnectionW {
        public Cn1(Connection w) { super(w); }
    }

    void bar(Cn1 cn1) {
        cn1.q("select 1").fetchNone();
    }

    @TestTemplate public void test() { }
}
