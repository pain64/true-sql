package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.OracleConnection;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.*;

import static net.truej.sql.compiler.TrueSqlTests.Database.ORACLE;
import static net.truej.sql.fetch.Parameters.Nullable;
import static net.truej.sql.fetch.Parameters.out;
import static net.truej.sql.test.__09__Types.*;

@ExtendWith(TrueSqlTests.class) @EnableOn(ORACLE)
@TrueSql public class __09__Types_ORACLE {
    @TestTemplate public void test(OracleConnection cn) {
        BOOLEAN.eq(
            v -> cn.q("select cast(? as boolean)", v).fetchOne(Nullable, Boolean.class),
            v -> cn.q("call p_boolean(?, ?)", v, out(Boolean.class))
                .asCall().fetchOne(Nullable, Boolean.class)
        );
        BYTE.eq(
            v -> cn.q("select cast(? as number(3, 0))", v).fetchOne(Nullable, Byte.class),
            v -> cn.q("call p_number(?, ?)", v, out(Byte.class))
                .asCall().fetchOne(Nullable, Byte.class)
        );
        SHORT.eq(
            v -> cn.q("select cast(? as number(5, 0))", v).fetchOne(Nullable, Short.class),
            v -> cn.q("call p_number(?, ?)", v, out(Short.class))
                .asCall().fetchOne(Nullable, Short.class)
        );
        INTEGER.eq(
            v -> cn.q("select cast(? as number(10, 0))", v).fetchOne(Nullable, Integer.class),
            v -> cn.q("{call p_int(?, ?)}", v, out(Integer.class))
                .asCall().fetchOne(Nullable, Integer.class)
        );
        LONG.eq(
            v -> cn.q("select cast(? as number(19, 0))", v).fetchOne(Nullable, Long.class),
            v -> cn.q("call p_number(?, ?)", v, out(Long.class))
                .asCall().fetchOne(Nullable, Long.class)
        );
        FLOAT.eq(
            v -> cn.q("select cast(? as binary_float)", v).fetchOne(Nullable, Float.class),
            v -> cn.q("call p_float(?, ?)", v, out(Float.class))
                .asCall().fetchOne(Nullable, Float.class)
        );
        DOUBLE.eq(
            v -> cn.q("select cast(? as binary_double)", v).fetchOne(Nullable, Double.class),
            v -> cn.q("call p_double(?, ?)", v, out(Double.class))
                .asCall().fetchOne(Nullable, Double.class)
        );
        BIG_DECIMAL.eq(
            v -> cn.q("select cast(? as number(15, 3))", v).fetchOne(Nullable, BigDecimal.class),
            v -> cn.q("call p_number(?, ?)", v, out(BigDecimal.class))
                .asCall().fetchOne(Nullable, BigDecimal.class)
        );
        STRING.eq(
            v -> cn.q("select cast(? as varchar(100))", v).fetchOne(Nullable, String.class),
            v -> cn.q("call p_varchar(?, ?)", v, out(String.class))
                .asCall().fetchOne(Nullable, String.class)
        );
        LOCAL_DATE.eq(
            v -> cn.q("select cast(? as date)", v).fetchOne(Nullable, LocalDate.class),
            v -> cn.q("call p_date(?, ?)", v, out(LocalDate.class))
                .asCall().fetchOne(Nullable, LocalDate.class)
        );
        LOCAL_DATE_TIME.eq(
            v -> cn.q("select cast(? as timestamp)", v).fetchOne(Nullable, LocalDateTime.class),
            v -> cn.q("{call p_timestamp(?, ?)}", v, out(LocalDateTime.class))
                .asCall().fetchOne(Nullable, LocalDateTime.class)
        );
        ZONED_DATE_TIME.eq(
            v -> cn.q("select cast(? as timestamp with time zone)", v).fetchOne(Nullable, ZonedDateTime.class),
            v -> cn.q("{call p_timestamptz(?, ?)}", v, out(ZonedDateTime.class))
                .asCall().fetchOne(Nullable, ZonedDateTime.class)
        );
        BYTE_ARRAY.eq(
            v -> cn.q("select cast(? as raw(32))", v).fetchOne(Nullable, byte[].class),
            v -> cn.q("{call p_raw(?, ?)}", v, out(byte[].class))
                .asCall().fetchOne(Nullable, byte[].class)
        );
//        _UUID.eq(
//            v -> cn.q("select cast(? as uniqueidentifier)", v).fetchOne(Nullable, UUID.class),
//            v -> cn.q("{call p_uniqueidentifier(?, ?)}", v, out(UUID.class))
//                .asCall().fetchOne(Nullable, UUID.class)
//        );
    }
}
