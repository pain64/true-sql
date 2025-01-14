package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.UserSex;
import net.truej.sql.test.__12__TypeBindingsG.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static net.truej.sql.compiler.TrueSqlTests.EnableOn;
import static net.truej.sql.fetch.Parameters.Nullable;

@ExtendWith(TrueSqlTests.class) @EnableOn(MYSQL)
@TrueSql public class __12__TypeBindingsCommon {

    @TestTemplate public void testEnumBind(MainDataSource ds) {
        Assertions.assertEquals(
            UserSex.MALE, ds.q("select 'MALE'")
                .fetchOne(Nullable, UserSex.class)
        );
    }

    @TestTemplate public void testTypeInference(MainDataSource ds) throws NoSuchFieldException {
        Object fetched = ds.q("select 'MALE' as \":t UserSex sex\"")
            .g.fetchOne(User.class);

        Assertions.assertEquals(
            UserSex.class.getName(),
            fetched.getClass().getField("sex").getType().getName()
        );
    }
}
