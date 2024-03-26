package com.truej.sql.v3.config;

public @interface TypeBinding {
    String sqlType();
    Class<?> javaClass();
    Class<? extends TypeRW> rw();
}
