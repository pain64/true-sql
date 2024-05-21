package com.truej.sql.v3.config;

import static com.truej.sql.v3.config.Configuration.NOT_DEFINED;

public @interface CompileTimeChecks {
    String url();
    String username() default NOT_DEFINED; // is null
    String password() default NOT_DEFINED; // is null
}
