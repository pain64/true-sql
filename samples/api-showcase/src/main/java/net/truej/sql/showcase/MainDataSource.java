package net.truej.sql.showcase;

import net.truej.sql.config.Configuration;
import net.truej.sql.source.DataSourceW;

import javax.sql.DataSource;

@Configuration public record MainDataSource(DataSource w) implements DataSourceW {
    // public record MainConnection(Connection w) implements ConnectionW {}
}
