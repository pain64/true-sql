package net.truej.sql.compiler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.truej.sql.util.CompileAssert.assertGenerated;

@Disabled
public class SourceFindTest {
    @Test void first() throws IOException {
        assertGenerated(
            """ 
                package xxx;
                import net.truej.sql.source.ConnectionW;
                import net.truej.sql.source.DataSourceW;
                import net.truej.sql.TrueSql;
                import net.truej.sql.config.Configuration;
                import net.truej.sql.config.CompileTimeChecks;
                import org.postgresql.geometric.PGpoint;
                import java.sql.Connection;
                import javax.sql.DataSource;
                
                @TrueSql class Simple {
                  @Configuration
                  record C1(Connection w) implements ConnectionW {}
                  @Configuration
                  record C2(Connection w) implements ConnectionW {}
                  record C3(Connection w) implements ConnectionW {}
                  record C4(Connection w) implements ConnectionW {}
                  @Configuration
                  record D5(DataSource w) implements DataSourceW {}
                
                  record ADto(String name, int age) {}

                  void v1(C1 cn) {
                    cn.q("select v from t1").fetchList(String.class);
                  }
                  C2 cn;
                  void v2() {
                    cn.q("select v from t1").fetchList(String.class);
                  }
                  void v3(Connection w) {
                    var cn = new C3(w);
                    cn.q("select v from t1").fetchList(String.class);
                  }
                  void v4() {
                    C4 cn = null;
                    cn.q("select v from t1").fetchList(String.class);
                  }
                  void v5(D5 ds) {
                    ds.withConnection(cn ->
                      cn.q("select v from t1").fetchList(String.class)
                    );
                  }
                  void v6(D5 ds) {
                    ds.withConnection((ConnectionW cn) ->
                      cn.q("select v from t1").fetchList(String.class)
                    );
                  }
                }"""
        );
    }
}
