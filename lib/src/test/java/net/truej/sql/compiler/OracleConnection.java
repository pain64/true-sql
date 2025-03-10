package net.truej.sql.compiler;

import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.ConnectionW;

import java.sql.Connection;

@Configuration(
    checks = @CompileTimeChecks( // NB: this values will be overriden in runtime for each database
        url = "jdbc:hsqldb:mem:db",
        username = "SA",
        password = ""
    )
)
public class OracleConnection extends ConnectionW {
    public OracleConnection(Connection w) { super(w); }
}
