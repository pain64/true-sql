package com.truej.sql.showcase;

import com.truej.sql.TrueSql;
import com.truej.sql.config.Database;

import javax.sql.DataSource;

@Database(name = "main")
public class MainDb extends TrueSql {
    public MainDb(DataSource ds) {
        super(ds, e -> e);
    }
}
