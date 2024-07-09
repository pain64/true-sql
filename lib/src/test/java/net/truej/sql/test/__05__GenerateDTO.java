package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.test.__05__GenerateDTOTrueSql.*;

@ExtendWith(TrueSqlTests.class)
@TrueSql public class __05__GenerateDTO {
    @Test public void test(MainConnection cn) {
        cn.q("select * from user").g.fetchList(User.class);
    }
}
