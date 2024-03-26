package com.truej.sql.v3.config;

public @interface CompileTimeChecks {
    String databaseUrl();
    Property[] connectionProperties() default {};
    String username() default "__MAGIC__TRUE_SQL_EMPTY_USERNAME"; // is null
    String password() default "__MAGIC__TRUE_SQL_EMPTY_PASSWORD"; // is null
}
