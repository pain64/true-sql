package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.ConnectionW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.unfold;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
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
