package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.inout;
import static net.truej.sql.fetch.Parameters.out;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(HSQLDB)
@TrueSqlTests2.Message(kind = ERROR, text = "only IN parameters allowed in batch mode")
@TrueSql
public class __37__InvocationsFinderBatchModeParameters {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q(List.of(1,2,3,4), """
            {call digit_magic(?,?,?)}
            """, number -> new Object[] {1, inout(number), out(Integer.class)}).asCall().fetchNone();
    }

}
