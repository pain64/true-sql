package net.truej.sql.compiler;

import net.truej.sql.source.DataSourceW;

import javax.sql.DataSource;

public record MainDataSource(DataSource w) implements DataSourceW { }
