package com.truej.sql.compiler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.truej.sql.util.CompileAssert.*;

public class DtoTypesTest {
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
                package xxx;
                import com.truej.sql.v3.source.ConnectionW;
                import com.truej.sql.v3.TrueSql;
                import com.truej.sql.v3.config.Configuration;
                import org.postgresql.geometric.PGpoint;
                import java.sql.Connection;
                
                @Configuration
                record MainConnection(Connection w) implements ConnectionW {}
                
                record ADto(String name, int age) {}
                
                @TrueSql class Simple {
                  void simple(MainConnection cn, String v) {
                    cn."insert into t1 values(1, \\{v})".fetchOne(String.class);
                    
                    cn."insert into t1 values(1, \\{v})".g.fetchOne(ADto.class);
                    
                    cn."insert into t1 values(2, \\{v})"
                      .withUpdateCount.g.fetchOne(PGpoint.class);
                      
                    cn."insert into t1 values(2, \\{v})"
                      .asCall()
                      .withUpdateCount.g.fetchOne(java.lang.String.class);
                      
                    cn."insert into t1 values(2, \\{v})"
                      .asGeneratedKeys("id")
                      .withUpdateCount.g.fetchOne(java.lang.String.class);
                      
                    cn."insert into t1 values(2, \\{v})"
                      .afterPrepare(s -> s.setFetchSize(9000))
                      .asGeneratedKeys("id")
                      .withUpdateCount.g.fetchOne(java.lang.String.class);
                  }
                }"""
        );
    }
}
