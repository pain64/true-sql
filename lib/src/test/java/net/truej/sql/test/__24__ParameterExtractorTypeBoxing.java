package net.truej.sql.test;


import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.TrueSqlTests2.EnableOn;
import net.truej.sql.compiler.UserSex;
import net.truej.sql.fetch.Parameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class) @EnableOn(MSSQL)
@TrueSql public class __24__ParameterExtractorTypeBoxing {

    record V(boolean f1, byte f2, short f3, int f4, long f5, float f6, double f7) { }

    @TestTemplate public void test1(MainConnection cn) {
        var instance = new V(true, (byte) 1, (short) 2, 3, 4L, 5f, 6d);
        Assertions.assertEquals(
            instance, cn.q(
                """
                    select
                        (cast(f1 as bit     )),
                        (cast(f2 as tinyint )),
                        (cast(f3 as smallint)),
                        (cast(f4 as int     )),
                        (cast(f5 as bigint  )),
                        (cast(f6 as real    )),
                        (cast(f7 as float   ))
                    from (values ?) as t(f1, f2, f3, f4, f5, f6, f7)
                    """,
                Parameters.unfold(
                    List.of(instance),
                    v -> new Object[]{v.f1, v.f2, v.f3, v.f4, v.f5, v.f6, v.f7}
                )
            ).fetchOne(V.class)
        );
    }
}
