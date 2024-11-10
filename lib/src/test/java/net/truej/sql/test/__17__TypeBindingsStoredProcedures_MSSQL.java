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
import java.time.LocalTime;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;
import static net.truej.sql.fetch.Parameters.out;

// FIXME: mariadb
// TODO: test for postgresql
@ExtendWith(TrueSqlTests2.class) @EnableOn(MSSQL)
@TrueSql public class __17__TypeBindingsStoredProcedures_MSSQL {
    // FIXME: dedup with __09__DefaultTypes
    record DataTypes(
        @NotNull BigDecimal bigDecimalType, @Nullable BigDecimal bigDecimalTypeNull,
        boolean booleanType, @Nullable Boolean booleanTypeNull,
        @NotNull LocalDate dateType, @Nullable LocalDate dateTypeNull,
        int integerType, @Nullable Integer integerTypeNull,
        long longType, @Nullable Long longTypeNull,
        @NotNull String stringType, @Nullable String stringTypeNull,
        short shortType, @Nullable Short shortTypeNull,
        byte byteType, @Nullable Byte byteTypeNull,
        @NotNull LocalTime timeType, @Nullable LocalTime timeTypeNull,
        @NotNull LocalDateTime timestampType, @Nullable LocalDateTime timestampTypeNull,
        float floatType, @Nullable Float floatTypeNull,
        double doubleType, @Nullable Double doubleTypeNull
    ) { }

    @TestTemplate public void test(MainDataSource ds) {
        // TODO: в схеме указано numeric(15, 3)
        // но драйвер вместо 100.241 привозит 100.2410
        var data = new DataTypes(
            new BigDecimal("100.2410"), null,
            true, null,
            //new byte[] {1}, null,
            LocalDate.of(2024, 7, 1), null,
            100, null,
            99L, null,
            "hello", null,
            (short) 255, null,
            (byte) 8, null,
            LocalTime.of(23, 59, 59), null,
            LocalDateTime.of(1984, 1, 1, 23, 59, 59), null,
            3.14159f, null,
            3.1415926535d, null
        );

        var fetched = ds.q("""
                {call test_types_procedure(
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?
                )}
                """,
            data.bigDecimalType, out(BigDecimal.class), out(BigDecimal.class),
            data.booleanType, out(boolean.class), out(Boolean.class),
            //new byte[] {1}, null,
            data.dateType, out(LocalDate.class), out(LocalDate.class),
            data.integerType, out(int.class), out(Integer.class),
            data.longType, out(long.class), out(Long.class),
            data.stringType, out(String.class), out(String.class),
            data.shortType(), out(short.class), out(Short.class),
            data.byteType, out(byte.class), out(Byte.class),
            data.timeType, out(LocalTime.class), out(LocalTime.class),
            data.timestampType, out(LocalDateTime.class), out(LocalDateTime.class),
            data.floatType, out(float.class), out(Float.class),
            data.doubleType, out(double.class), out(Double.class)
        ).asCall().fetchOne(DataTypes.class);

        Assertions.assertEquals(data, fetched);
    }
}
