package net.truej.sql.compiler;

import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.source.DataSourceW;

import javax.sql.DataSource;
@Configuration(
        checks = @CompileTimeChecks(
                url = "jdbc:hsqldb:mem:db",
                username = "SA",
                password = ""
//        url = "jdbc:postgresql://localhost:5432/uikit_sample",
//        username = "uikit",
//        password = "1234"
        )
) public record MainDataSource(DataSource w) implements DataSourceW { }

// net.truej.sql.source.DataSourceW$1
// net.truej.sql.source.DataSourceW