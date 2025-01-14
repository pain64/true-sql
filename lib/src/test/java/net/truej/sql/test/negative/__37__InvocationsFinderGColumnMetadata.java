package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.negative.__30__GenerateDtoTrueSql.Dto3;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.unfold;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(HSQLDB)
@TrueSqlTests.Message(kind = ERROR, text = ".g mode is not available because column metadata is " +
                                           "not provided by JDBC driver")
@TrueSql public class __37__InvocationsFinderGColumnMetadata {
    @TestTemplate public void test(MainDataSource ds) {
        ds.q("insert into city(name) values ('Moskau')")
            .asGeneratedKeys("id").g.fetchOne(Dto3.class);
    }
}
