package net.truej.sql.compiler;

import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.ConnectionW;
import net.truej.sql.source.DataSourceW;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;

@Configuration(
    checks = @CompileTimeChecks(
        url = "jdbc:postgresql://localhost:5432/truesqldb",
        username = "sa",
        password = ""
    )
) public record PgConnection(Connection w) implements ConnectionW { }
