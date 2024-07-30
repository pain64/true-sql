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
//        url = "jdbc:postgresql://localhost:5432/uikit_sample",
//        username = "uikit",
//        password = "1234"
    )
) public class MainConnection extends ConnectionW {
    public MainConnection(Connection w) { super(w); }
}
