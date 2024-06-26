package com.truej.sql.compiler;

import com.truej.sql.v3.compiler.TrueSqlAnnotationProcessor;
import com.truej.sql.v3.config.TypeReadWrite;
import com.truej.sql.v3.fetch.UpdateResult;
import com.truej.sql.v3.prepare.Parameters;
import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;
import com.truej.sql.v3.source.Source;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static com.truej.sql.v3.compiler.GLangParser.*;
import static com.truej.sql.v3.compiler.StatementGenerator.*;
import static com.truej.sql.v3.compiler.TrueSqlAnnotationProcessor.*;

public class StatementGeneratorTest {
    @Disabled
    @Test void batch() {
        Assertions.assertEquals(
            """
                
                """,
            generate(
                12,
                SourceMode.CONNECTION,
                new BatchedQuery(List.of(
                    new TextPart("select name from users where id = "),
                    new SimpleParameter(null),
                    new TextPart("")
                )),
                new AsDefault(),
                new FetchList(new ScalarType(true, "java.lang.String")),
                true
            )
        );
    }

    interface ParameterExtractor<B, P, E extends Exception> {
        P get(B element) throws E;
    }

    @Disabled
    @Test void singleUnfold() {
        Assertions.assertEquals(
            """
                
                """,
            generate(
                12,
                SourceMode.DATASOURCE,
                new SingleQuery(List.of(
                    new TextPart("select name from users where id = "),
                    new SimpleParameter(null),
                    new TextPart("and (name, age) in ("),
                    new UnfoldParameter(2, null),
                    new TextPart(")")
                )),
                new AsDefault(),
                new FetchList(new ScalarType(true, "java.lang.String")),
                true
            )
        );
    }
}
