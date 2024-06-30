package com.truej.sql.compiler;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.truej.sql.util.CompileAssert.assertGenerated;

public class SourceFindTest {
    @Test void first() throws IOException {
        assertGenerated(
            """ 
                package xxx;
                import com.truej.sql.v3.source.ConnectionW;
                import com.truej.sql.v3.source.DataSourceW;
                import com.truej.sql.v3.TrueSql;
                import com.truej.sql.v3.config.Configuration;
                import com.truej.sql.v3.config.CompileTimeChecks;
                import org.postgresql.geometric.PGpoint;
                import java.sql.Connection;
                import javax.sql.DataSource;
                
                @Configuration
                record C1(Connection w) implements ConnectionW {}
                @Configuration
                record C2(Connection w) implements ConnectionW {}
                record C3(Connection w) implements ConnectionW {}
                record C4(Connection w) implements ConnectionW {}
                @Configuration
                record D5(DataSource w) implements DataSourceW {}
                
                record ADto(String name, int age) {}
                
                @TrueSql class Simple {
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
