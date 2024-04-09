package com.truej.sql.v3.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface Configuration {
    CompileTimeChecks checks()
        default @CompileTimeChecks(databaseUrl = "");

    TypeBinding[] typeBindings() default {};
}
