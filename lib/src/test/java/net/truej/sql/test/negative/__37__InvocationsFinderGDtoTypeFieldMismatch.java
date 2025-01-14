package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.*;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(POSTGRESQL)
@TrueSqlTests.Message(kind = ERROR, text = "sql type id (java.sql.Types) mismatch for column 1 " +
                                           "(for generated dto field `:t UserSex usersex`). Expected 12 but has 4")
@TrueSql public class __37__InvocationsFinderGDtoTypeFieldMismatch {
    @TestTemplate
    public void test(MainDataSource ds) {
        ds.q("""
                select 1 as ":t UserSex usersex"
                """).g.fetchList(Dto4.class);
    }
}
