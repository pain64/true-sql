package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class)
// @TrueSqlTests2.Message(
//    kind = ERROR, text = "Aggired"
//)
@TrueSqlTests2.EnableOn(POSTGRESQL) @Disabled
@TrueSql public class __32__BadFormatException {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("select 1").g.fetchOne(__30__GenerateDtoTrueSql.BadTypeDto.class);
    }
}
