package com.truej.sql.config;

public @interface ReadJavaClass {
    Class<?> javaClass();
    Class<? extends DefaultReader> reader();
}
