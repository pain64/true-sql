package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.compiler.UserSex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.geometric.PGpoint;


import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;
import static net.truej.sql.source.Parameters.*;

import net.truej.sql.test.__12__TypeBindingsTrueSql.*;

@ExtendWith(TrueSqlTests2.class) @EnableOn(POSTGRESQL)
@TrueSql public class __12__TypeBindings {

    @TestTemplate public void testPointBind(MainDataSource ds) {
        var expected = new PGpoint(1, 1);

        Assertions.assertEquals(
            expected, ds.q("select ?::point", expected).fetchOne(PGpoint.class)
        );
    }

    @TestTemplate public void testEnumBind(MainDataSource ds) {
        Assertions.assertEquals(
            UserSex.MALE, ds.q("select sex from users where id = 1")
                .fetchOne(Nullable, UserSex.class)
        );
    }

    @TestTemplate public void testTypeInference(MainDataSource ds) throws NoSuchFieldException {
        Object fetched = ds.q("select name, sex from users where id = 1")
            .g.fetchOne(User.class);

        Assertions.assertEquals(
            UserSex.class.getName(),
            fetched.getClass().getField("sex").getType().getName()
        );
    }
}
