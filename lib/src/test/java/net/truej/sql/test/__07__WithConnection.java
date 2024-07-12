package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __07__WithConnection {

    @TestTemplate @DisabledOn(POSTGRESQL) public void test(MainDataSource ds) {
        ds.withConnection(cn ->
            cn.q("""
                insert into names2(name) values ('Joe'), ('Donald')"""
            ).fetchNone()
        );
    }
}
