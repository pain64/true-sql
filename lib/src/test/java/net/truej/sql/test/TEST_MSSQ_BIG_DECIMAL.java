package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.source.ConnectionW;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static net.truej.sql.compiler.TrueSqlTests2.Database.MSSQL;


@ExtendWith(TrueSqlTests2.class) @TrueSqlTests2.EnableOn(MSSQL)
@TrueSql public class TEST_MSSQ_BIG_DECIMAL {
    @TestTemplate
    public void test(MainConnection cn) throws SQLException {
        // TODO: в схеме указано numeric(15, 3)
        // но драйвер вместо 100.241 привозит 100.2410
        var connection = cn.w;

        var stmt = connection.createStatement();
        stmt.execute("""
            create procedure test_bigdecimal
                @big_decimal_type      decimal(15, 5)       ,
                @big_decimal_type_o    decimal(15, 5) output
               as begin
                   set @big_decimal_type_o = @big_decimal_type;
               end;
            """);
        var call = connection.prepareCall("""
            {call test_bigdecimal(100.24112, ?)}""");
        call.registerOutParameter(1, Types.DECIMAL);
        call.execute();

        var actual = call.getBigDecimal(1);

        var expected = new BigDecimal("100.24112");

        Assertions.assertEquals(expected, actual);
    }
}
