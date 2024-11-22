package net.truej.sql.test.negative;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.function.IntBinaryOperator;

import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(POSTGRESQL) @Disabled
@TrueSql public class __28__ExistingDTOParser {
    public record BadDTO (
        List<List<Integer>> field
    ) {}

//    public class BadClass {
//        List<List<Integer>> field;
//        BadClass(List<List<Integer>> field) {
//            this.field = field;
//        }
//    }
//    interface Cool {}
//    public record BadDTO2 (Cool me) {}
//    //public record City(Long id, String name) { }

    @TestTemplate public void test(MainDataSource ds) {
        //wtf
        ds.q("select 1").fetchOne(BadDTO.class);
    }

//    @TestTemplate public void test2(MainDataSource ds) {
//        ds.q("select 1").fetchOne(BadDTO2.class);
//    }
}
