package net.truej.sql.compiler;

import net.truej.sql.bindings.UuidReadWrite;
import net.truej.sql.config.CompileTimeChecks;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.source.ConnectionW;

import java.sql.Connection;

@Configuration(
    checks = @CompileTimeChecks( // NB: this values will be overriden in runtime for each database
        url = "jdbc:hsqldb:mem:db",
        username = "SA",
        password = ""
    ),
    typeBindings = @TypeBinding(
        compatibleSqlTypeName = "uniqueidentifier",
        rw = UuidReadWrite.class
    )
) public class MssqlConnection extends ConnectionW {
    public MssqlConnection(Connection w) { super(w); }
}
