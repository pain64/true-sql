package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.TrueSqlTests2.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.unfold;

@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "Raw list cannot be used as Dto or Dto field")
@TrueSql public class __39__ExistingDtoListCannotBeDto {
    @TestTemplate public void test(MainConnection cn) {
        cn.q("values 1").fetchOne(List.class);
    }
}
