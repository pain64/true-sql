package com.truej.sql.config;

public @interface Database {
    String name();
    ReadSqlType[] readSqlTypeMappings() default {};
    ReadJavaClass[] readJavaClassMappings() default {};
    WriteJavaClass[] writeJavaClassMappings() default {};
}
