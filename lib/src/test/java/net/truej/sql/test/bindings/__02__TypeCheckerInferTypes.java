package net.truej.sql.test.bindings;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.bindings.__02__TypeCheckerInferTypesG.*;

import java.time.OffsetTime;

import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(POSTGRESQL)
@TrueSql public class __02__TypeCheckerInferTypes {
    @TestTemplate public void testTimeTypes(MainConnection cn) throws NoSuchFieldException, IllegalAccessException {
        var result = cn.q("""
            select
                '2023-07-14'::date as date,
                '10:30:00'::time as time,
                '2023-07-14 10:30:00'::timestamp as timestamp,
                '10:30:00 UTC'::timetz as timetz
            """).g.fetchOne(XXX.class);

//        var result = cn.q("""
//            select
//                offset_time_type from all_default_data_types
//            """).g.fetchOne(V1.class);

        var ttz = result.getClass().getField("timetz");
        ttz.setAccessible(true);
        Assertions.assertEquals(
            OffsetTime.class, ttz.get(result).getClass()
        );
    }
}
