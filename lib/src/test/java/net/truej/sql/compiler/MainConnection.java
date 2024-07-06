package net.truej.sql.compiler;

import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.ConnectionW;

import java.sql.Connection;

@Configuration(
    checks = @CompileTimeChecks(
        url = "jdbc:postgresql://localhost:5432/uikit_sample",
        username = "uikit",
        password = "1234"
    )
) public record MainConnection(Connection w) implements ConnectionW { }
