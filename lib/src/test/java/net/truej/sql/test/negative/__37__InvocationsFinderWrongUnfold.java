package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.unfold;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(HSQLDB)
@TrueSqlTests2.Message(kind = ERROR, text = "Unfold parameter extractor must be lambda literal returning object array" +
    " literal (e.g. `u -> new Object[]{u.f1, u.f2}`)")
@TrueSql public class __37__InvocationsFinderWrongUnfold {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("""
            select 1 where 5 in (?)
            """, unfold(List.of(1,2,3), null)).fetchNone();
    }
}
