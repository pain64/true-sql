package com.truej.sql.compiler;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.truej.sql.util.CompileAssert.*;

public class Test1 {
    // Анализ AST
    //   Stmt."select * from xxx"
    //       .with | withGeneratedKeys
    //       .fetchXXX(ds, m | g(String.class)) | fetchUpdateCount ->
    // 1. по ds | connection понять что это за база (найти @Configuration) (withTransaction, withConnection)
    // 2. найти query
    // 3. найти таргет для маппера (m | g)
    // 4. safe unsafe (g недоступен, требуется аннотация на метод)
    //   1.
    @Test void first() throws IOException {
        assertGenerated(
            """  
                import static com.truej.sql.v3.TrueSql.*;
                import com.truej.sql.v3.source.ConnectionW;
                import com.truej.sql.v3.TrueSql;
                import com.truej.sql.v3.config.Configuration;
                import java.sql.Connection;
                
                @Configuration
                record MainConnection(Connection w) implements ConnectionW {}
                
                @TrueSql.Process class Simple {
                  void simple(MainConnection cn, String v) {
                    com.truej.sql.v3.TrueSql.m(String.class);
                    com.truej.sql.v3.TrueSql.g(String.class);
                    
                    Stmt."insert into t1 values(1, \\{v})"
                        .withGeneratedKeys("id")
                        .fetchOne(cn, m(Long.class));
                  }
                }"""
        );

    }
}
