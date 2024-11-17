package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.fetch.Parameters.inout;
import static net.truej.sql.fetch.Parameters.out;

@ExtendWith(TrueSqlTests2.class) @EnableOn(ORACLE)
@TrueSql public class __17__TypeBindingsStoredProcedures_ORACLE {
    record DataTypes(
        @NotNull BigDecimal bigDecimalType, @Nullable BigDecimal bigDecimalTypeNull,
        boolean booleanType, @Nullable Boolean booleanTypeNull,
        @NotNull LocalDate dateType, @Nullable LocalDate dateTypeNull,
        int integerType, @Nullable Integer integerTypeNull,
        long longType, @Nullable Long longTypeNull,
        @NotNull String stringType, @Nullable String stringTypeNull,
        short shortType, @Nullable Short shortTypeNull,
        byte byteType, @Nullable Byte byteTypeNull,
        @NotNull LocalDateTime timestampType, @Nullable LocalDateTime timestampTypeNull,
        float floatType, @Nullable Float floatTypeNull,
        double doubleType, @Nullable Double doubleTypeNull,
        @NotNull ZonedDateTime zonedDateTime, @Nullable ZonedDateTime zonedDateTimeNull
        ) { }

    @TestTemplate public void test(MainDataSource ds) {
        // FIXME: add assert
        var data = new DataTypes(
            new BigDecimal("100.24124"), null,
            true, null,
            LocalDate.of(2024, 7, 1), null,
            100, null,
            99L, null,
            "hello", null,
            (short) 255, null,
            (byte) 8, null,
            LocalDateTime.of(1984, 1, 1, 23, 59, 59), null,
            3.14159f, null,
            3.1415926535d, null,
            ZonedDateTime.of(
                2024,11,17,
                20,0,0,0,
                ZoneId.of("Europe/Paris")),
            null
        );

        var fetched = ds.q("""
                {call test_types_procedure(
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?,
                    ?,?
                )}
                """,
            inout(data.bigDecimalType), out(BigDecimal.class),
            inout(data.booleanType), out(Boolean.class),
            inout(data.dateType), out(LocalDate.class),
            inout(data.integerType), out(Integer.class),
            inout(data.longType), out(Long.class),
            inout(data.stringType), out(String.class),
            inout(data.shortType), out(Short.class),
            inout(data.byteType), out(Byte.class),
            inout(data.timestampType), out(LocalDateTime.class),
            inout(data.floatType), out(Float.class),
            inout(data.doubleType), out(Double.class),
            inout(data.zonedDateTime), out(ZonedDateTime.class)
        ).asCall().fetchOne(DataTypes.class);

        Assertions.assertEquals(fetched, data);
    }
}
