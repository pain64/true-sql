package net.truej.sql.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface Configuration {
    String STRING_NOT_DEFINED = "__MAGIC__TRUE_SQL_PROPERTY_NOT_DEFINED";
    int INT_NOT_DEFINED = 1996;

    CompileTimeChecks checks()
        default @CompileTimeChecks(url = STRING_NOT_DEFINED);

    TypeBinding[] typeBindings() default {};
    // TODO ???
    // nullableAnnotation = Class<?>
    // notNullAnnotation  = Class<?>
}
