package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.*;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.*;

@ExtendWith(TrueSqlTests2.class) @EnableOn({POSTGRESQL})
@TrueSql public class __09__DefaultTypesPostgresql {

    record DataTypes(
        BigDecimal bigDecimalType, @Nullable BigDecimal bigDecimalTypeNull,
        boolean booleanType, @Nullable Boolean booleanTypeNull,
        LocalDate dateType, @Nullable LocalDate dateTypeNull,
        int integerType, @Nullable Integer integerTypeNull,
        long longType, @Nullable Long longTypeNull,
        String stringType, @Nullable String stringTypeNull,
        //Byte []  bytes_type, @Nullable Byte [] bytes_type_null,
        short shortType, @Nullable Short shortTypeNull,
        LocalTime timeType, @Nullable LocalTime timeTypeNull,
        LocalDateTime timestampType, @Nullable LocalDateTime timestampTypeNull,
        float floatType, @Nullable Float floatTypeNull,
        double doubleType, @Nullable Double doubleTypeNull
    ) { }

    // FIXME: https://trello.com/c/1PEhNxDa/95-%D0%B1%D0%B5%D0%B4%D0%B0-%D1%81-testdatasourcegetconnection
    @TestTemplate public void test(MainDataSource ds) {
        ds.withConnection(cn -> {
            var data = new DataTypes(
                new BigDecimal("100.241"), null,
                true, null,
                //new byte[] {1}, null,
                LocalDate.of(2024, 7, 1), null,
                100, null,
                99L, null,
                "hello", null,
                (short) 255, null,
                LocalTime.of(23, 59, 59), null,
                LocalDateTime.of(1984, 1, 1, 23, 59, 59), null,
                3.14159f, null,
                3.1415926535d, null
            );
            cn.q("""
                    insert into all_default_data_types values(
                        ?, ?,
                        ?, ?,
                    
                        ?, ?,
                        ?, ?,
                        ?, ?,
                        ?, ?,
                        ?, ?,
                        ?, ?,
                        ?, ?,
                        ?, ?,
                        ?, ?)
                    """,
                data.bigDecimalType, data.bigDecimalTypeNull,
                data.booleanType, data.booleanTypeNull,
                data.dateType, data.dateTypeNull,
                data.integerType, data.integerTypeNull,
                data.longType, data.longTypeNull,
                data.stringType, data.stringTypeNull,
                //Byte []  bytes_type, @Nullable Byte [] bytes_type_null,
                data.shortType, data.shortTypeNull,
                data.timeType, data.timeTypeNull,
                data.timestampType, data.timestampTypeNull,
                data.floatType, data.floatTypeNull,
                data.doubleType, data.doubleTypeNull

            ).fetchNone();

            var fetched = cn.q("""
                select
                    bigdecimal_type, bigdecimal_type_null ,
                    boolean_type, boolean_type_null,
                    --bytearray_type bytea NOT NULL,
                    --bytearray_type_null bytea,
                    date_type, date_type_null,
                    integer_type, integer_type_null,
                    long_type, long_type_null,
                    string_type, string_type_null,
                    short_type, short_type_null,
                    time_type, time_type_null,
                    timestamp_type, timestamp_type_null,
                    float_type, float_type_null,
                    double_type, double_type_null
                from all_default_data_types
                """
            ).fetchOne(DataTypes.class);

            Assertions.assertEquals(data, fetched);
            return null;
        });
    }
}
