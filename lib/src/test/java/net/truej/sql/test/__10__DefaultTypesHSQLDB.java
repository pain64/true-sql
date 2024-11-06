package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests2;
import net.truej.sql.test.__05__GenerateDtoTrueSql.TypeTest;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.*;

import static net.truej.sql.compiler.TrueSqlTests2.*;
import static net.truej.sql.compiler.TrueSqlTests2.Database.HSQLDB;
import static net.truej.sql.compiler.TrueSqlTests2.Database.POSTGRESQL;

@Disabled
@ExtendWith(TrueSqlTests2.class) @EnableOn(HSQLDB)
@TrueSql public class __10__DefaultTypesHSQLDB {
    record DataTypes(
        byte byte_type, @Nullable Byte byte_null,
        OffsetTime offsetTime, @Nullable OffsetTime offsetTimeNullable
    ) { }

    @TestTemplate public void test(MainDataSource ds) {
        ds.q("""
                insert into all_default_data_types values(
                    ?, ?,
                    ?, ?
                )
                """,
            (byte) 8, (Byte) null,
            OffsetTime.of(23, 59, 59, 0,ZoneOffset.ofHours(0)), null
        ).fetchNone();


        // FIXME: assert ???
        ds.q("""
            select
                byte_type, byte_type_null,
                time_offset_type, time_offset_type_null
            from all_default_data_types
            """
        ).fetchOne(DataTypes.class);
    }


    @TestTemplate public void test2(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals(
            "",
            new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    ds.q("""
                select
                    gg as "name",
                    bool as " NewType en.bool",
                    byte as "         en.byte",
                    c as "         en.c",
                    short as "         en.short",
                    intt as "         en.intt",
                    long as "         en.long",
                    flo as "         en.flo",
                    doub as "         en.doub"
                from grouped_dto""").g.fetchOne(TypeTest.class)
                )
        );
    }
}
