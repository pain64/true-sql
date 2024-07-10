package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.PgConnection;
import net.truej.sql.compiler.PgDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(TrueSqlTests.class)
@TrueSql
public class __07__WithConnection {

    @Test public void test(MainDataSource ds) {
        ds.withConnection(cn -> {
            cn.q("""
                        insert into names2(name) values ('Joe'), ('Donald')
            """).fetchNone();

            return null;
        });
    }

}
