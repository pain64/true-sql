package com.truej.sql.showcase;

import com.truej.sql.v3.config.Configuration;
import com.truej.sql.v3.source.ConnectionW;
import com.truej.sql.v3.source.DataSourceW;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration public record MainDataSource(DataSource w) implements DataSourceW {
    public record MainConnection(Connection w) implements ConnectionW {}
}