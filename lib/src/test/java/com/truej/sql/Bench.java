package com.truej.sql;

import com.truej.sql.v3.fetch.FetcherOne;
import com.truej.sql.v3.prepare.Transform;
import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.SqlExceptionR;
import com.truej.sql.v3.fetch.TooFewRowsException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.SampleTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class Bench {
    public static void main(String[] args) throws RunnerException {
        var opt = new OptionsBuilder()
            .include(Bench.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }

    record MainConnection(Connection w) implements ConnectionW { }

    @State(Scope.Thread)
    public static class S {
        final MainConnection connection;

        // -Xcomp -XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*.trueSql

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public S() {
            // connection = new FakeConnection();
            try {
                connection = new MainConnection(
                    DriverManager.getConnection("jdbc:hsqldb:mem:xxx", "SA", "")
                );

                connection.w.createStatement().execute("""
                    create table users (id bigint, name varchar(64), email varchar(64));
                    """);
                connection.w.createStatement().execute("""
                    insert into users values (42, 'Joe', 'example@email.com');
                    """);

//                connection.w.createStatement().execute("""
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//                        insert into users values (1, 'Joe', 'example@email.com');
//
//                        insert into users values (42, 'Joe', 'example@email.com');
//                    """);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }

    @Benchmark public long trueSql(S state) {
        return FetcherOne.fetch(
            Transform.value(),
            Test1.Generated.stmt_20_12(state.connection, new Test1.LongArgument(42)),
            Test1.Generated.mapper_30_10()
        );

//        try (
//            var s = Test1.Generated.stmt_20_12(
//                new Test1.LongArgument(42)
//            ).fetchStream(state.connection, Test1.Generated.mapper_30_10())
//        ) {
//            return s.mapToLong(v -> v).toArray();
//        }
    }

    @Benchmark public long rawJdbc(S state) throws SQLException {
        try (
            var stmt = state.connection.w
                .prepareStatement("select id from users where id = ?")
        ) {
            stmt.setLong(1, 42);
            stmt.execute();
            var rs = stmt.getResultSet();
            if (!rs.next())
                throw new TooFewRowsException();

            return rs.getLong(1);
        }

//        try (
//            var stmt = state.connection.w
//                .prepareStatement("select id from users where id != ?")
//        ) {
//            var result = new long[100]; // "super optimal"
//            stmt.setLong(1, 42);
//            stmt.execute();
//            var rs = stmt.getResultSet();
//            var i = 0;
//
//            while(rs.next()) {
//                result[i] = rs.getLong(1);
//                i++;
//            }
//
//            return result;
//            //return rs.getLong(1);
//        }
    }
}
