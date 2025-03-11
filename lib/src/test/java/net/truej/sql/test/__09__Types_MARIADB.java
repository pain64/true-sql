package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MariaDbConnection;
import net.truej.sql.compiler.TrueSqlTests;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static net.truej.sql.compiler.TrueSqlTests.Database.MARIADB;
import static net.truej.sql.compiler.TrueSqlTests.EnableOn;
import static net.truej.sql.fetch.Parameters.Nullable;
import static net.truej.sql.fetch.Parameters.out;
import static net.truej.sql.test.__09__Types.*;

@ExtendWith(TrueSqlTests.class) @EnableOn(MARIADB)
@TrueSql public class __09__Types_MARIADB {
    @TestTemplate public void test(MariaDbConnection cn) {
        BOOLEAN.eq(
            v -> cn.q("select f_boolean(?)", v).fetchOne(Nullable, Boolean.class),
            v -> v
//            v -> cn.q("call p_boolean(?, ?)", v, out(Boolean.class))
//                .asCall().fetchOne(Nullable, Boolean.class)
        );
        BYTE.eq(
            v -> cn.q("select f_tinyint(?)", v).fetchOne(Nullable, Byte.class),
            v -> cn.q("call p_tinyint(?, ?)", v, out(Byte.class))
                .asCall().fetchOne(Nullable, Byte.class)
        );
        SHORT.eq(
            v -> cn.q("select f_smallint(?)", v).fetchOne(Nullable, Short.class),
            v -> cn.q("call p_smallint(?, ?)", v, out(Short.class))
                .asCall().fetchOne(Nullable, Short.class)
        );
        INTEGER.eq(
            v -> cn.q("select f_int(?)", v).fetchOne(Nullable, Integer.class),
            v -> cn.q("call p_int(?, ?)", v, out(Integer.class))
                .asCall().fetchOne(Nullable, Integer.class)
        );
        LONG.eq(
            v -> cn.q("select f_bigint(?)", v).fetchOne(Nullable, Long.class),
            v -> cn.q("call p_bigint(?, ?)", v, out(Long.class))
                .asCall().fetchOne(Nullable, Long.class)
        );
        FLOAT.eq(
            v -> cn.q("select f_float(?)", v).fetchOne(Nullable, Float.class),
            v -> cn.q("call p_float(?, ?)", v, out(Float.class))
                .asCall().fetchOne(Nullable, Float.class)
        );
        DOUBLE.eq(
            v -> cn.q("select f_double(?)", v).fetchOne(Nullable, Double.class),
            v -> cn.q("call p_double(?, ?)", v, out(Double.class))
                .asCall().fetchOne(Nullable, Double.class)
        );
        BIG_DECIMAL.eq(
            v -> cn.q("select cast(? as decimal(15, 3))", v).fetchOne(Nullable, BigDecimal.class),
            v -> cn.q("call p_decimal(?, ?)", v, out(BigDecimal.class))
                .asCall().fetchOne(Nullable, BigDecimal.class)
        );
        STRING.eq(
            v -> cn.q("select f_varchar(?)", v).fetchOne(Nullable, String.class),
            v -> cn.q("call p_varchar(?, ?)", v, out(String.class))
                .asCall().fetchOne(Nullable, String.class)
        );
        LOCAL_DATE.eq(
            v -> cn.q("select f_date(?)", v).fetchOne(Nullable, LocalDate.class),
            v -> cn.q("call p_date(?, ?)", v, out(LocalDate.class))
                .asCall().fetchOne(Nullable, LocalDate.class)
        );
        LOCAL_TIME.eq(
            v -> cn.q("select f_time(?)", v).fetchOne(Nullable, LocalTime.class),
            v -> cn.q("call p_time(?, ?)", v, out(LocalTime.class))
                .asCall().fetchOne(Nullable, LocalTime.class)
        );
        LOCAL_DATE_TIME.eq(
            v -> cn.q("select f_datetime(?)", v).fetchOne(Nullable, LocalDateTime.class),
            v -> cn.q("call p_datetime(?, ?)", v, out(LocalDateTime.class))
                .asCall().fetchOne(Nullable, LocalDateTime.class)
        );
//        OFFSET_DATE_TIME.eq(
//            v -> cn.q("select cast(? as datetimeoffset)", v).fetchOne(Nullable, OffsetDateTime.class),
//            v -> cn.q("{call p_datetimeoffset(?, ?)}", v, out(OffsetDateTime.class))
//                .asCall().fetchOne(Nullable, OffsetDateTime.class)
//        );
//        BYTE_ARRAY.eq(
//            v -> cn.q("select f_varbinary(?)", v).fetchOne(Nullable, byte[].class),
//            v -> cn.q("call p_varbinary(?, ?)", v, out(byte[].class))
//                .asCall().fetchOne(Nullable, byte[].class)
//        );
        _UUID.eq(
            v -> cn.q("select f_uuid(?)", v).fetchOne(Nullable, UUID.class),
            v -> cn.q("call p_uuid(?, ?)", v, out(UUID.class))
                .asCall().fetchOne(Nullable, UUID.class)
        );
    }
}