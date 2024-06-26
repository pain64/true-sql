package com.truej.sql.compiler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.truej.sql.util.CompileAssert.assertGenerated;

public class QueryCheckTest {
    @Disabled
    @Test void first() throws IOException {
        assertGenerated(
            """ 
                package xxx;
                import com.truej.sql.v3.source.ConnectionW;
                import static com.truej.sql.v3.prepare.Parameters.*;
                import com.truej.sql.v3.prepare.Parameters.*;
                import com.truej.sql.v3.TrueSql;
                import com.truej.sql.v3.Group;
                import com.truej.sql.v3.config.Configuration;
                import com.truej.sql.v3.config.CompileTimeChecks;
                import java.util.List;
                import java.sql.Connection;
                import lombok.EqualsAndHashCode;
                import lombok.AllArgsConstructor;
                
                @EqualsAndHashCode @AllArgsConstructor class AAA {
                  final int x;
                  final int y;
                  final @Group List<String> z;
                }
                
                @Configuration(checks = @CompileTimeChecks(
                    url = "jdbc:postgresql://localhost:5432/uikit_sample",
                    username = "uikit",
                    password = "1234"
                ))
                record C1(Connection w) implements ConnectionW {}
                
                @TrueSql class Simple {
                  void v1(C1 cn) {
                    cn."select v from t1 where id = \\{42}".fetchOne(String.class);
                    cn."call transfer(1, \\{inout(2)})".asCall().fetchNone();
                    
                    cn."select v from t1 where id in (\\{unfold(List.of(1, 2, 3))})"
                       .fetchList(String.class);
                    
                    var pairs = List.of(
                        new Pair<>(1, "a"), new Pair<>(2, "b")
                    );
                       
                    cn."select v from t1 where (id, v) in (\\{unfold2(pairs)})"
                       .fetchList(String.class);                       
                  }
                }"""
        );
    }
}
