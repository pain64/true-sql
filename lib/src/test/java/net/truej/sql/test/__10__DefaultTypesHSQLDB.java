package net.truej.sql.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.test.__05__GenerateDtoG.TypeTest;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.*;

import static net.truej.sql.compiler.TrueSqlTests.*;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __10__DefaultTypesHSQLDB {
    record DataTypes(
        byte byte_type, @Nullable Byte byte_null,
        OffsetTime offsetTime, @Nullable OffsetTime offsetTimeNullable
    ) { }

    @TestTemplate public void test(MainConnection cn) {
        cn.q("""
                insert into all_default_data_types values(
                    ?, ?,
                    ?, ?
                )
                """,
            (byte) 8, (Byte) null,
            OffsetTime.of(23, 59, 59, 0, ZoneOffset.ofHours(0)), null
        ).fetchNone();


        // FIXME: assert ???
        cn.q("""
            select
                byte_type, byte_type_null,
                time_offset_type, time_offset_type_null
            from all_default_data_types
            """
        ).fetchOne(DataTypes.class);
    }


    @TestTemplate public void test2(MainDataSource ds) throws JsonProcessingException {
        Assertions.assertEquals(
            "null", new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writerWithDefaultPrettyPrinter().writeValueAsString(
                    ds.q("""
                        select
                            gg    as "        name   ",
                            bool  as "NewType   en.f1",
                            byte  as "          en.f2",
                            short as "          en.f3",
                            intt  as "          en.f4",
                            long  as "          en.f5",
                            flo   as "          en.f6",
                            doub  as "          en.f7"
                        from grouped_dto""").g.fetchOneOrZero(TypeTest.class)
                )
        );
    }
}
