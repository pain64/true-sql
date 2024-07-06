package net.truej.sql.compiler;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.truej.sql.util.CompileAssert.assertGenerated;

public class ParametersBindTest {
    @Test void first() throws IOException {
        assertGenerated(
            """ 
                package xxx;
                import net.truej.sql.source.ConnectionW;
                import net.truej.sql.source.DataSourceW;
                import static net.truej.sql.source.Parameters.*;
                import net.truej.sql.source.Parameters;
                import net.truej.sql.source.Parameters.*;
                import net.truej.sql.TrueSql;
                import net.truej.sql.config.Configuration;
                import net.truej.sql.config.CompileTimeChecks;
                import org.postgresql.geometric.PGpoint;
                import java.util.List;
                import java.sql.Connection;
                import javax.sql.DataSource;
                
                record C1(Connection w) implements ConnectionW {}
                
                @TrueSql class Simple {
                  void v1(C1 cn) {
                    cn.q("select v from t1 where id = ?", 42).fetchOne(String.class);
                    cn.q("? = call p1(1, ?)", out(), inout(2)).asCall().fetchNone();
                    cn.q("? = call p1(1, ?)", out(), Parameters.inout(2)).asCall().fetchNone();

                    var pairs = List.of(
                        new Pair<>(1, "a"), new Pair<>(2, "b")
                    );
                    cn.q("select v from t1 where (id, v) in = (?)", unfold2(pairs))
                       .fetchList(String.class);
                  }
                }"""
        );
    }
}