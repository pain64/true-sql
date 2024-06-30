package com.truej.sql.v3.source;

public sealed interface Source extends RuntimeConfig
    permits DataSourceW, ConnectionW { }