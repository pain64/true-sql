package com.truej.sql.config;

public @interface WriteJavaClass {
    Class<?> javaClass();
    Class<? extends Writer> writer();
}
