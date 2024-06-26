package com.truej.sql.v3.config;

import static com.truej.sql.v3.config.Configuration.NOT_DEFINED;

public @interface TypeBinding {
    /** from java.sql.Types */
    int compatibleSqlType();
    String compatibleSqlTypeName() default NOT_DEFINED;
    boolean mayBeNullable() default true;
    Class<? extends TypeReadWrite<?>> rw();
}
