package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TrueSqlTests2.class)
@TrueSql public class __14__AllowedSourcePatterns {
    MainConnection classField;

    @TestTemplate public void allowed(MainConnection parameter) {
        classField = parameter;
        var local = new MainConnection(parameter.w);

        classField.q("select name from users").fetchNone();
        parameter.q("select name from users").fetchNone();
        local.q("select name from users").fetchNone();
    }

    MainDataSource dsClassField;

    @TestTemplate public void allowed(MainDataSource parameter) {
        dsClassField = parameter;

        dsClassField.inTransaction(cn ->
            cn.q("select name from users").fetchNone()
        );
    }
}
