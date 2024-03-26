package com.truej.sql;

import com.truej.sql.v3.*;
import com.truej.sql.v3.TrueSql;
import com.truej.sql.v3.config.Configuration;
import com.truej.sql.v3.fetch.*;
import com.truej.sql.v3.prepare.Statement;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Iterator;
import java.util.List;

import static com.truej.sql.v3.TrueSql.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test1 {

    record PgConnection(Connection w) implements ConnectionW { }
    @Configuration record PgDataSource(DataSource w) implements DataSourceW { }

    static DataSource createDs() { return null; }

    public static void main(String[] args) {
        var pg = new PgDataSource(createDs());
    }

    // @Database class PgDataSource extends DataSourceW {}
    // @Database class PgConnection extends ConnectionW {}
    // @Database class OracleDataSource extends DbConfig {}
    // void main () {
    //    var pg = new PgDatabase(new DataSource(...))
    //    pg.withConnection()
    //    var oracle = new OracleDatabase(new DataSource(...))
    //
    //    Stmt."select v from t1".fetchOne(pg.ds, m(String.class))
    // }
    // // void doSome(DataSource pg) {
    //    //     Stmt."select v from t1".fetchOne(pg, m(String.class))
    //    // }
    // record Databases(
    //     @Database() DataSource pg,
    //     @Database() DataSource oracle
    // ) {}
    //public class Databases {
    //   public final @Database() DataSource pg;
    //   public final @Database() DataSource oracle;
    //
    //   public Databases(DataSource pg, DataSource oracle) {
    //       this.pg = pg;
    //       this.oracle = oracle;
    //   }`````````````````````
    //}
    // void doSome(Databases db) {
    //     Stmt."select v from t1".fetchOne(db.pg, m(String.class))
    // }
    // @Database() DataSource ds
    interface Argument {
        void set(int i, PreparedStatement stmt) throws SQLException;
    }

    record ObjectArgument(Object arg) implements Argument {
        @Override public void set(int i, PreparedStatement stmt) throws SQLException {
            stmt.setObject(i, arg);
        }
    }

    record LongArgument(long arg) implements Argument {
        @Override public void set(int i, PreparedStatement stmt) throws SQLException {
            stmt.setLong(i, arg);
        }
    }

    // Generated code
    static class Generated {
        static Statement stmt_20_12(Argument a1) {
            return new Statement() {
                @Override public RuntimeException mapException(SQLException e) {
                    return new SqlExceptionR(e);
                }
                @Override public String query() {
                    return "select id from users where id != ?";
                }
                @Override public void bindArgs(PreparedStatement stmt) throws SQLException {
                    a1.set(1, stmt);
                }
            };
        }

        static <H> ResultSetMapper<Long, H> mapper_30_10() {
            return new ResultSetMapper<>() {
                @Override @Nullable public H hints() {
                    return null;
                }

                @Override public Iterator<Long> map(ResultSet rs) {
                    return new Iterator<>() {
                        @Override public boolean hasNext() {
                            try {
                                return rs.next();
                            } catch (SQLException e) {
                                throw new SqlExceptionR(e);
                            }
                        }
                        @Override public Long next() {
                            try {
                                return rs.getLong(1);
                            } catch (SQLException e) {
                                throw new SqlExceptionR(e);
                            }
                        }
                    };
                }
            };
        }
    }

    // TODO: make compiler plugin & emit code
    // TODO: check that debugger works fine
    // TODO: use JMH for test that it erased find
    // TODO: PreparedStatement.setFetchSize
    // TODO: orIsNull pattern


    record MainConnection(Connection w) implements ConnectionW { }

    @Test void test1() throws SQLException {
        var cn = new MainConnection(
            DriverManager.getConnection("jdbc:hsqldb:mem:xxx", "SA", "")
        );

        cn.w.createStatement().execute("""
                create table users (id bigint, name varchar(64), email varchar(64));
            """);

        cn.w.createStatement().execute("""
                insert into users values (42, 'Joe', 'example@email.com');
            """);

        @Nullable String p1 = null;
        // (f1 = p1 or p1 is null)
        // ! null на Java
        // f1 = p1
        // null на Java
        // "1 = 1"
        // select * from t1 where
        // \{p1 == null ? "1 = 1" : Part."f1 = \{p1}"}
//        Stmt."""
//            select * from t1 where
//                \{StmtParameters.staticNull("f1 = ", p1)}
//                f1 = \{p1} or \{StmtParameters.staticNull(p1, "1 = 1")}
//            """.fetchList(cn, m(String.class));

        // 1. Remove overloads
        // 2. adjust prepared statement
        // var call = conn.prepareCall();
        // var params = Sql."select name from users where id = \{ 42 }"
        // params.bind(call)
        // call.execute()
        // FetchOne.fetch(call, m(String))

//        var x1 = Stmt."update users set name = 'igor'"
//            .fetch(cn, stmt -> {
//                var v1 = FetcherList.apply(stmt.getGeneratedKeys(), m(Long.class));
//                var v2 = FetcherList.apply(stmt.getResultSet(), m(User.class));
//
//                stmt.getMoreResults();
//
//                var v3 = FetcherList.apply(stmt.getResultSet(), m(String.class));
//
//                return new Triple(v1, v2, v3);
//            });

        //.fetchGeneratedKeys(cn, rs -> FetcherList.apply(rs, m(Long.class)));

        TrueSql.batchStmt(
                List.of("a", "b", "c"),
                x -> Stmt."insert into t1(v) values(\{x})"
            )
            .withGeneratedKeys()
            .afterPrepare(s -> s.setFetchSize(42))
            //.fetchUpdateCount(cn, stmt -> FetcherList.apply(stmt, m(String.class)));
            .fetchUpdateCount(
                cn, new FetcherGeneratedKeys<>(
                    new FetcherList<>(m(String.class))
                )
            );

//        var xxx = Stmt."select name from users where id = \{42}"
//            .withGeneratedKeys()
//            .fetchUpdateCount(cn, stmt -> FetcherList.apply(stmt, m(String.class)))
//            .fetchGeneratedKeys(cn, rs -> FetcherList.apply(rs, m(String.class)))
//            .fetchUpdateCount(cn, stmt -> FetcherOne.apply(stmt, m(String.class)));
        //.fetchUpdateCount(cn, FetcherNone::apply)
        //.fetchUpdateCount(cn, FetcherStream.instance(m(String.class)));
        //.fetchUpdateCount(cn, stmt -> FetcherOne.fetch(stmt, m(String.class)));
        // setFetchSize ???
        // .fetch(rs -> {
        //     var d1 = FetcherList.fetch(rs.getResultSet, m(Long.class));
        //     rs.getMoreResults();
        //     var d2 = FetcherList.fetch(rs.getResultSet, m(String.class));
        //     return new Pair<>(d1, d2);
        // });
        //  .fetchUpdateCount(
        //      cn, stmt -> FetcherOne.fetch(stmt, m(String.class))
        //   )
        //  .fetchUpdateCount(cn, FetcherNone::fetch)
        //  .fetchUpdateCount(cn)
        //  .fetchUpdateCount(cn, FetcherOne.of(m(String.class))

        // translates to
        // кодогенрация?
//        Generated.stmt_20_11(42)
//            .fetchOne(cn, Generated.mapper_30_10());
//
//        Generated.stmt_20_12(
//            new ObjectArgument(42)
//        ).fetchOne(cn, Generated.mapper_30_10());

        // Will be patched to
        var name = Generated.stmt_20_12(
            new LongArgument(42)
        ).fetchOne(cn, Generated.mapper_30_10());

        assertEquals(name, "Joe");

        // pure JDBC version


        try (
            var stmt = cn.w.prepareStatement("select name from users where id = ?")
        ) {
            stmt.setLong(1, 42);
            stmt.execute();
            // stmt.getUpdateCount();
            var rs = stmt.getResultSet();
            rs.next();

            name = rs.getLong(1);
        }

        assertEquals(name, "Joe");
    }
}
