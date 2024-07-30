package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.MSSQL;
import static net.truej.sql.source.Parameters.*;

@ExtendWith(TrueSqlTests2.class)
@DisabledOn({HSQLDB, MSSQL})
@TrueSql public class __14__UnfoldMany {
    record User(long id, String name) { }

    @TestTemplate public void unfoldMany2(MainDataSource ds) {
        var data = List.of(
            new User(1L, "Joe"),
            new User(2L, "Donald")
        );

        Assertions.assertEquals(
            List.of("null", "Do not disturb"),
            ds.q("""
                    select case when info is null then 'null' else info end as info
                    from users where (id, name) in (?)""",
                unfold(data, u -> new Object[]{u.id, u.name})
            ).fetchList(Nullable, String.class)
        );
    }
}
