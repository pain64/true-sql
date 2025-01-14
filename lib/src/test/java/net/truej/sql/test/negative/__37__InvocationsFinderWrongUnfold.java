package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.compiler.TrueSqlTests.Message;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.unfold;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@Message(kind = ERROR, text = "Unfold parameter extractor must be lambda literal returning object array" +
                              " literal (e.g. `u -> new Object[]{u.f1, u.f2}`)")
@TrueSql public class __37__InvocationsFinderWrongUnfold {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("""
            select 1 where 5 in (?)
            """, unfold(List.of(1,2,3), null)).fetchNone();
    }
}
