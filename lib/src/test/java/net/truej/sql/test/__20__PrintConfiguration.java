package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.ContainsOutput;
import net.truej.sql.compiler.TrueSqlTests2.Env;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.ConnectionW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.SQLException;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@ContainsOutput("""
    TrueSql configuration:
    	truesql.net.truej.sql.test.__20__PrintConfiguration_.TestConnection.url=jdbc:hsqldb:mem:xxx
    	truesql.net.truej.sql.test.__20__PrintConfiguration_.TestConnection.username=null
    	truesql.net.truej.sql.test.__20__PrintConfiguration_.TestConnection.password=null"""
)
@Env(key = "truesql.printConfig", value = "true")
@TrueSql public class __20__PrintConfiguration {

    @Configuration(checks = @CompileTimeChecks(
        url = "jdbc:hsqldb:mem:xxx"
    ))
    static class TestConnection extends ConnectionW {
        public TestConnection(Connection w) { super(w); }
    }

    @TestTemplate public void test() throws SQLException { }
}
