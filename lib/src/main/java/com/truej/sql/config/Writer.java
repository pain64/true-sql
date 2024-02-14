package com.truej.sql.config;

import org.jetbrains.annotations.Nullable;

public interface Writer {
    String[] allowedSqlTypes();
    String generateWriteStatement(String fieldSelector, @Nullable String sqlType);
}
