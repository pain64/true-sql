package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.test.negative.GeneratedDTOTrueSql.PointSome;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.geometric.PGpoint;

import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;

@ExtendWith(TrueSqlTests2.class)
//@TrueSqlTests2.Message(
//    kind = ERROR, text = "target type implies 1 columns but result has 3"
//)
@TrueSql public class __20__ConflicNullableG {
//    @TestTemplate @TrueSqlTests2.DisabledOn(HSQLDB)
//    public void test(MainConnection cn) {
//        var expected = new PGpoint(1,1);
//        cn.q("""
//            select
//            'hello' as "message",
//            ?::point as ":t? p"
//            """, expected).g.fetchOne(PointSome.class);
//    }
}
