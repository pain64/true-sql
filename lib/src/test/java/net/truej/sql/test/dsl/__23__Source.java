package net.truej.sql.test.dsl;

import net.truej.sql.TrueSql;
import net.truej.sql.compiler.TrueSqlTests;
import net.truej.sql.source.ConnectionW;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import net.truej.sql.source.DataSourceW;
import static net.truej.sql.compiler.TrueSqlTests.Database.HSQLDB;

@ExtendWith(TrueSqlTests.class) @TrueSqlTests.EnableOn(HSQLDB)
@TrueSql public class __23__Source {
    @TestTemplate public void DataSourceWTest() {
        var u0 = new DataSourceW.AsCall();
        var u1 = new DataSourceW.AsCall.WithUpdateCount();
        var u2 = new DataSourceW.AsCall.WithUpdateCount.G();
        var u3 = new DataSourceW.AsCall.G();

        var s0 = new DataSourceW.Single();
        var s1 = new DataSourceW.Single.G();
        var s2 = new DataSourceW.Single.AsGeneratedKeys();
        var s3 = new DataSourceW.Single.WithUpdateCount();
        var s4 = new DataSourceW.Single.AsGeneratedKeys.G();
        var s5 = new DataSourceW.Single.AsGeneratedKeys.WithUpdateCount();
        var s6 = new DataSourceW.Single.AsGeneratedKeys.WithUpdateCount.G();
        var s7 = new DataSourceW.Single.WithUpdateCount.G();

        var b0 = new DataSourceW.Batched();
        var b1 = new DataSourceW.Batched.G();
        var b2 = new DataSourceW.Batched.AsGeneratedKeys();
        var b3 = new DataSourceW.Batched.WithUpdateCount();
        var b4 = new DataSourceW.Batched.AsGeneratedKeys.G();
        var b5 = new DataSourceW.Batched.AsGeneratedKeys.WithUpdateCount();
        var b6 = new DataSourceW.Batched.AsGeneratedKeys.WithUpdateCount.G();
        var b7 = new DataSourceW.Batched.WithUpdateCount.G();
    }

    @TestTemplate public void ConnectionWTest() {
        var s0 = new ConnectionW.Single();
        var s1 = new ConnectionW.Single.G();
        var s2 = new ConnectionW.Single.AsGeneratedKeys();
        var s3 = new ConnectionW.Single.WithUpdateCount();
        var s4 = new ConnectionW.Single.AsGeneratedKeys.G();
        var s5 = new ConnectionW.Single.AsGeneratedKeys.WithUpdateCount();
        var s6 = new ConnectionW.Single.AsGeneratedKeys.WithUpdateCount.G();
        var s7 = new ConnectionW.Single.WithUpdateCount.G();

        var b1 = new ConnectionW.Batched();
    }
}
