package net.truej.sql.source;

import net.truej.sql.dsl.NewConstraint;

public sealed interface Source extends RuntimeConfig, NewConstraint
    permits ConnectionW, DataSourceW { }
