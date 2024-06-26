package com.truej.sql.v3.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface Configuration {
    String NOT_DEFINED = "__MAGIC__TRUE_SQL_PROPERTY_NOT_DEFINED";

    CompileTimeChecks checks()
        default @CompileTimeChecks(url = NOT_DEFINED); // is null

    TypeBinding[] typeBindings() default {};
    // TODO ???
    // nullableAnnotation = Class<?>
    // notNullAnnotation  = Class<?>
}
