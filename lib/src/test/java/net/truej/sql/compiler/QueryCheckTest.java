package net.truej.sql.compiler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.truej.sql.util.CompileAssert.assertGenerated;

public class QueryCheckTest {
    @Test void first() throws IOException {
        assertGenerated(
            """ 
                package xxx;
                import net.truej.sql.source.ConnectionW;
                import static net.truej.sql.source.Parameters.*;
                import net.truej.sql.source.Parameters.*;
                import net.truej.sql.TrueSql;
                import net.truej.sql.config.Configuration;
                import net.truej.sql.config.CompileTimeChecks;
                import java.util.List;
                import java.sql.Connection;
                import lombok.EqualsAndHashCode;
                import lombok.AllArgsConstructor;
                
                @TrueSql class Simple {
                 @EqualsAndHashCode @AllArgsConstructor class AAA {
                  final int x;
                  final int y;
                  final List<String> z;
                }
                
                @Configuration(checks = @CompileTimeChecks(
                    url = "jdbc:postgresql://localhost:5432/uikit_sample",
                    username = "uikit",
                    password = "1234"
                ))
                record C1(Connection w) implements ConnectionW {}
                
                  void v1(C1 cn) {
                    cn.q("select v from t1 where id = ?", 42).fetchOne(Nullable, String.class);

                    cn.q("call transfer(1, ?)", 2).fetchNone();

                    cn.q("select v from t1 where id in (?)", unfold(List.of(1, 2, 3)))
                       .fetchList(Nullable, String.class);

                    var pairs = List.of(
                        new Pair<>(1, "a"), new Pair<>(2, "b")
                    );

                    cn.q("select v from t1 where (id, v) in (?)", unfold2(pairs))
                       .fetchList(Nullable, String.class);
                  }
                }"""
        );
    }
}
