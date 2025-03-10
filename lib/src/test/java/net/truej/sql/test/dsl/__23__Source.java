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
        new DataSourceW.AsCall();
        new DataSourceW.AsCall.WithUpdateCount();
        new DataSourceW.AsCall.WithUpdateCount.G();
        new DataSourceW.AsCall.G();

        new DataSourceW.Single();
        new DataSourceW.Single.G();
        new DataSourceW.Single.AsGeneratedKeys();
        new DataSourceW.Single.WithUpdateCount();
        new DataSourceW.Single.AsGeneratedKeys.G();
        new DataSourceW.Single.AsGeneratedKeys.WithUpdateCount();
        new DataSourceW.Single.AsGeneratedKeys.WithUpdateCount.G();
        new DataSourceW.Single.WithUpdateCount.G();

        new DataSourceW.Batched();
        new DataSourceW.Batched.G();
        new DataSourceW.Batched.AsGeneratedKeys();
        new DataSourceW.Batched.WithUpdateCount();
        new DataSourceW.Batched.AsGeneratedKeys.G();
        new DataSourceW.Batched.AsGeneratedKeys.WithUpdateCount();
        new DataSourceW.Batched.AsGeneratedKeys.WithUpdateCount.G();
        new DataSourceW.Batched.WithUpdateCount.G();
    }
}
