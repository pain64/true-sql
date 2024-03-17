package com.truej.sql;

import com.truej.sql.v3.SqlExceptionR;
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

    @State(Scope.Thread)
    public static class S {
        final Connection connection;

        // -Xcomp -XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*.trueSql

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public S() {
            // connection = new FakeConnection();
            try {
                connection = DriverManager
                    .getConnection("jdbc:hsqldb:mem:xxx", "SA", "");

                connection.createStatement().execute("""
                        create table users (id bigint, name varchar(64), email varchar(64));
                    """);

                connection.createStatement().execute("""
                        insert into users values (42, 'Joe', 'example@email.com');
                    """);
            } catch (SQLException e) {
                throw new SqlExceptionR(e);
            }
        }
    }

    @Benchmark public String trueSql(S state) {
        return Test1.Generated.stmt_20_12(
            new Test1.LongArgument(42)
        ).fetchOne(state.connection, Test1.Generated.mapper_30_10());
    }

    @Benchmark public String rawJdbc(S state) throws SQLException {
        try (
            var stmt = state.connection
                .prepareStatement("select name from users where id = ?")
        ) {
            stmt.setLong(1, 42);
            stmt.execute();
            var rs = stmt.getResultSet();
            rs.next();

            return rs.getString(1);
        }
    }
}
