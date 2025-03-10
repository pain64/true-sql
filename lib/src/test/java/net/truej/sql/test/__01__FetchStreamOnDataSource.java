package net.truej.sql.test;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.MainConnection;
import net.truej.sql.compiler.MainDataSource;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.compiler.TrueSqlTests.EnableOn;
import net.truej.sql.fetch.EvenSoNullPointerException;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;
import static net.truej.sql.fetch.Parameters.NotNull;
import static net.truej.sql.fetch.Parameters.Nullable;


@ExtendWith(TrueSqlTests.class) @EnableOn(HSQLDB)
@TrueSql public class __01__FetchStreamOnDataSource {
    @TestTemplate public void test(MainDataSource ds) {
        var allClinics = List.of(
            "Paris Neurology Hospital", "London Heart Hospital", "Diagnostic center"
        );

        try (
            var result = ds.q("select name from clinic")
                .fetchStream(String.class)
        ) {
            Assertions.assertEquals(allClinics, result.toList());
        }
    }
}

