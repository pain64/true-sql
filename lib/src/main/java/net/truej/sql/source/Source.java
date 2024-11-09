package net.truej.sql.source;

import net.truej.sql.config.RuntimeConfig;
import net.truej.sql.fetch.NewConstraint;

public sealed interface Source extends RuntimeConfig, NewConstraint
    permits ConnectionW, DataSourceW { }
