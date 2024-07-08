package net.truej.sql.source;

public sealed interface Source extends RuntimeConfig permits ConnectionW, DataSourceW { }
