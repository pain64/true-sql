package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests.EnableOn;
import static net.truej.sql.compiler.TrueSqlTests.Message;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(
    kind = ERROR, text = "Dto field of primitive type cannot be marked as @Nullable"
)
@Message(
    kind = ERROR, text = "Dto field of primitive type not needed to be marked as @NotNull"
)
@TrueSql public class __25__DtoFieldPrimitiveTypeNullability {
    record R1(@Nullable int x) { }
    record R2(@NotNull int x) { }

    @TestTemplate public void test(MainConnection cn) {
        cn.q("values 1").fetchOne(R1.class);
        cn.q("values 1").fetchOne(R2.class);
    }
}
