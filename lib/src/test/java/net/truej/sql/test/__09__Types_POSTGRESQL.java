package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.*;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.*;
import static net.truej.sql.fetch.Parameters.Nullable;
import static net.truej.sql.fetch.Parameters.out;
import static net.truej.sql.test.__09__Types.*;

@ExtendWith(TrueSqlTests.class) @EnableOn(POSTGRESQL)
@TrueSql public class __09__Types_POSTGRESQL {
    @TestTemplate public void test(MainDataSource ds) {
        // bytearray ???

        BOOLEAN.eq(
            v -> ds.q("select cast(? as bool)", v).fetchOne(Nullable, Boolean.class),
            v -> ds.q("call p_bool(?, ?)", v, out(Boolean.class))
                .asCall().fetchOne(Nullable, Boolean.class)
        );
        SHORT.eq(
            v -> ds.q("select cast(? as smallint)", v).fetchOne(Nullable, Short.class),
            v -> ds.q("call p_smallint(?, ?)", v, out(Short.class))
                .asCall().fetchOne(Nullable, Short.class)
        );
        INTEGER.eq(
            v -> ds.q("select cast(? as int)", v).fetchOne(Nullable, Integer.class),
            v -> ds.q("call p_int(?, ?)", v, out(Integer.class))
                .asCall().fetchOne(Nullable, Integer.class)
        );
        LONG.eq(
            v -> ds.q("select cast(? as bigint)", v).fetchOne(Nullable, Long.class),
            v -> ds.q("call p_bigint(?, ?)", v, out(Long.class))
                .asCall().fetchOne(Nullable, Long.class)
        );
        FLOAT.eq(
            v -> ds.q("select cast(? as real)", v).fetchOne(Nullable, Float.class),
            v -> ds.q("call p_real(?, ?)", v, out(Float.class))
                .asCall().fetchOne(Nullable, Float.class)
        );
        DOUBLE.eq(
            v -> ds.q("select cast(? as float)", v).fetchOne(Nullable, Double.class),
            v -> ds.q("call p_float(?, ?)", v, out(Double.class))
                .asCall().fetchOne(Nullable, Double.class)
        );
        BIG_DECIMAL.eq(
            v -> ds.q("select cast(? as decimal(15, 3))", v).fetchOne(Nullable, BigDecimal.class),
            v -> ds.q("call p_decimal(?, ?)", v, out(BigDecimal.class))
                .asCall().fetchOne(Nullable, BigDecimal.class)
        );
        STRING.eq(
            v -> ds.q("select cast(? as varchar)", v).fetchOne(Nullable, String.class),
            v -> ds.q("call p_varchar(?, ?)", v, out(String.class))
                .asCall().fetchOne(Nullable, String.class)
        );
        LOCAL_DATE.eq(
            v -> ds.q("select cast(? as date)", v).fetchOne(Nullable, LocalDate.class),
            v -> v
//            v -> ds.q("call p_date(?, ?)", v, out(LocalDate.class))
//                .asCall().fetchOne(Nullable, LocalDate.class)
        );
        LOCAL_TIME.eq(
            v -> ds.q("select cast(? as time)", v).fetchOne(Nullable, LocalTime.class),
            v -> v
//            v -> ds.q("call p_time(?, ?)", v, out(LocalTime.class))
//                .asCall().fetchOne(Nullable, LocalTime.class)
        );
        LOCAL_DATE_TIME.eq(
            v -> ds.q("select cast(? as timestamp)", v).fetchOne(Nullable, LocalDateTime.class),
            v -> v
//            v -> ds.q("call p_timestamp(?, ?)", v, out(LocalDateTime.class))
//                .asCall().fetchOne(Nullable, LocalDateTime.class)
        );
        OFFSET_DATE_TIME.eq(
            v -> ds.q("select cast(? as timestamptz)", v).fetchOne(Nullable, OffsetDateTime.class),
            v -> v
//            v -> ds.q("call p_timestamptz(?, ?)", v, out(OffsetDateTime.class))
//                .asCall().fetchOne(Nullable, OffsetDateTime.class)
        );
        OFFSET_TIME.eq(
            v -> ds.q("select cast(? as timetz)", v).fetchOne(Nullable, OffsetTime.class),
            v -> v
//            v -> ds.q("call p_timetz(?, ?)", v, out(OffsetTime.class))
//                .asCall().fetchOne(Nullable, OffsetTime.class)
        );
        _UUID.eq(
            v -> ds.q("select cast(? as uuid)", v).fetchOne(Nullable, UUID.class),
            v -> ds.q("call p_uuid(?, ?)", v, out(UUID.class))
                .asCall().fetchOne(Nullable, UUID.class)
        );
    }
}