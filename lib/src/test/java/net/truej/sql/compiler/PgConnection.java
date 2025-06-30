package net.truej.sql.compiler;

import net.truej.sql.bindings.UuidReadWrite;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.source.ConnectionW;
import net.truej.sql.source.DataSourceW;

import javax.sql.DataSource;
import java.sql.Connection;

// FIXME: remove ???
@Configuration(
    checks = @CompileTimeChecks(
        url = "jdbc:postgresql://localhost:5432/truesqldb",
        username = "sa",
        password = "1234"
    ),
    typeBindings = @TypeBinding(
        compatibleSqlTypeName = "uuid",
        rw = UuidReadWrite.class
    )
) public class PgConnection extends ConnectionW {
    public PgConnection(Connection w) { super(w); }
}
