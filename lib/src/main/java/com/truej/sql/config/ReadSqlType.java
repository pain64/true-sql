package com.truej.sql.config;

public @interface ReadSqlType {
    String typeName();
    Class<? extends Reader> reader();
}
