package net.truej.sql.compiler;

import net.truej.sql.bindings.UuidReadWrite;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.source.ConnectionW;

import java.sql.Connection;

@Configuration(
    typeBindings = @TypeBinding(
        compatibleSqlTypeName = "uuid",
        rw = UuidReadWrite.class
    )
) public class HsqldbConnection extends ConnectionW {
    public HsqldbConnection(Connection w) { super(w); }
}
