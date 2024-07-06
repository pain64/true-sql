package net.truej.sql.config;

import static net.truej.sql.config.Configuration.STRING_NOT_DEFINED;

public @interface CompileTimeChecks {
    String url();
    String username() default STRING_NOT_DEFINED; // is null
    String password() default STRING_NOT_DEFINED; // is null
}
