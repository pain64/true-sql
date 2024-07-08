package net.truej.sql.compiler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.truej.sql.util.CompileAssert.*;

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
                import net.truej.sql.source.ConnectionW;
                import net.truej.sql.config.TypeBinding;
                import net.truej.sql.bindings.AsObjectReadWrite;
                import static net.truej.sql.source.Parameters.Nullable;
                import net.truej.sql.TrueSql;
                import net.truej.sql.config.Configuration;
                import org.postgresql.geometric.PGpoint;
                import java.sql.Connection;
                import java.util.List;
                import net.truej.sql.source.NoopInvocation;
                
                
                @TrueSql class Test {
                  public static class PgPointReadWrite extends AsObjectReadWrite<PGpoint> { }
                
                  @Configuration(
                      typeBindings = {
                          @TypeBinding(rw = PgPointReadWrite.class)
                      }
                  )
                  record MainConnection(Connection w) implements ConnectionW {}
                
                  record ADto(String name, int age) {}
                  record BDto(long id, List<String> names) {}
                
                  record User(long id, String name) {}
                
                  void simple(MainConnection cn, String v) {

                    cn.q("insert into t1 values(1, ?)", v)
                        .fetchOne(String.class);

                    cn.q("insert into t1 values(1, ?)", v).fetchOne(Nullable, String.class);

                    cn.q("insert into t1 values(1, ?)", v).fetchOne(ADto.class);

                    // FIXME
                    // cn.q("select id, name from users").fetchList(BDto.class);

                    // cn.q("insert into t1 values(1, ?)", v).g.fetchOne(NewDto.class);

// FIXME
//                    cn.q("insert into t1 values(2, ?)", v)
//                      .withUpdateCount.fetchOne(PGpoint.class);

//                    cn.q("insert into t1 values(2, ?)", v)
//                      .asCall()
//                      .withUpdateCount.fetchOne(java.lang.String.class);

                    cn.q("insert into t1 values(2, ?)", v)
                      .asGeneratedKeys("id")
                      .withUpdateCount.fetchOne(java.lang.String.class);

//                    cn.q("insert into t1 values(2, ?)", v)
//                      // .afterPrepare(s -> s.setFetchSize(9000))
//                      .asGeneratedKeys("id")
//                      .withUpdateCount.fetchOne(java.lang.String.class);

//                    cn.q("insert into t1 values(2, ?)", v)
//                      // .afterPrepare(s -> s.setFetchSize(9000))
//                      .asGeneratedKeys(1)
//                      .withUpdateCount.fetchOne(java.lang.String.class);

                    var users = List.of(new User(1L, "Joe"));

                    cn.q(
                      users, "insert into t1 values(?, ?)",
                      u -> new Object[]{u.id, u.name}
                    ).asGeneratedKeys("id").fetchList(Long.class);
                  }
                }"""
        );
    }
}
