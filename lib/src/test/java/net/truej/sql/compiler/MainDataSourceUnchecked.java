package net.truej.sql.compiler;

import net.truej.sql.source.DataSourceW;

import javax.sql.DataSource;

public class MainDataSourceUnchecked extends DataSourceW {
    public MainDataSourceUnchecked(DataSource w) { super(w); }
}
