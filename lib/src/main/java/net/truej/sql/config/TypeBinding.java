package net.truej.sql.config;

public @interface TypeBinding {
    /** from java.sql.Types */
    int compatibleSqlType() default Configuration.INT_NOT_DEFINED;
    String compatibleSqlTypeName() default Configuration.STRING_NOT_DEFINED;
    boolean mayBeNullable() default true;
    Class<? extends TypeReadWrite<?>> rw();
}