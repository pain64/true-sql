package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@ExtendWith(TrueSqlTests2.class) @DisabledOn(POSTGRESQL)
@TrueSql public class __16__GeneratedKeysIndicies {
    @TestTemplate public void test(MainConnection cn) {
        Assertions.assertNull(
            cn.q("insert into city values(5, 'Moscow')")
                .asGeneratedKeys(1).fetchNone()
        );
    }
}
