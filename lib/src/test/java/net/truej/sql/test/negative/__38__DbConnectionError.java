package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.ConnectionW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.unfold;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "No suitable driver found for jdbcc:hsqldb:mem:xxx")
@TrueSql public class __38__DbConnectionError {

    @Configuration(checks = @CompileTimeChecks(
        url = "jdbcc:hsqldb:mem:xxx"
    ))
    public static class BadConnection extends ConnectionW {
        public BadConnection(Connection w) { super(w); }
    }

    @TestTemplate public void test() {
        var cn = new BadConnection(null);
        cn.q("select 1").fetchNone();
    }
}
