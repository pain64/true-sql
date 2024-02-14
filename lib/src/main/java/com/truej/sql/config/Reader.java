package com.truej.sql.config;

public interface Reader {
    Class<?>[] allowedToJavaClasses();
    Class<?> defaultToJavaClass();
    String generateReadExpression(String columnNumber, Class<?> toJavaClass);
}
