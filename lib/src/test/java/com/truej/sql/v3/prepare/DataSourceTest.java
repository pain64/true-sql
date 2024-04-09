package com.truej.sql.v3.prepare;

import com.truej.sql.fetch.Fixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

public class DataSourceTest {
    @Test void withConnection() throws SQLException {
        Fixture.withDataSource(ds ->
            Assertions.assertEquals(
                ds.withConnection(cn ->
                    Fixture.queryStmt("values(1)")
                        .fetchList(cn, Fixture.longMapper(null))
                ), List.of(1L)
            )
        );
    }

    @Test void inTransaction() throws SQLException {
        Fixture.withDataSource(ds ->
            Assertions.assertEquals(
                ds.withConnection(cn ->
                    Fixture.queryStmt("values(1)")
                        .fetchList(cn, Fixture.longMapper(null))
                ), List.of(1L)
            )
        );
    }
}
