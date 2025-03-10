package net.truej.sql.compiler;

import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.ConnectionW;

import java.sql.Connection;

@Configuration(
    checks = @CompileTimeChecks(
        url = "jdbc:hsqldb:mem:db",
        username = "SA",
        password = ""
    )
) public class MainConnection extends ConnectionW {
    public MainConnection(Connection w) { super(w); }
}
