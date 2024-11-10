package net.truej.sql.test.negative.bindings;

import net.truej.sql.TrueSql;
import net.truej.sql.bindings.StringReadWrite;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeBinding;
import net.truej.sql.source.ConnectionW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.Types;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import static net.truej.sql.compiler.TrueSqlTests2.Message;
import static net.truej.sql.fetch.Parameters.NotNull;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text =
    "For source net.truej.sql.test.negative.bindings.__03__TypeAndTypeNameBothSpecified_.Cn1: " +
    "compatibleSqlType and compatibleSqlTypeName cannot be specified at the same time"
)
@TrueSql public class __03__TypeAndTypeNameBothSpecified {

    @Configuration(
        typeBindings = @TypeBinding(
            compatibleSqlType = Types.VARCHAR,
            compatibleSqlTypeName = "varchar2",
            rw = StringReadWrite.class
        )
    ) static class Cn1 extends ConnectionW {
        public Cn1(Connection w) { super(w); }
    }

    void bar(Cn1 cn1) {
        cn1.q("select 1").fetchNone();
    }

    @TestTemplate public void test(MainConnection cn) { }
}
