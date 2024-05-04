package com.truej.sql.v3.prepare;

import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ManagedAction<P extends PreparedStatement, R, T> {
    boolean willStatementBeMoved();
    T apply(
        RuntimeConfig conf, R executionResult, P stmt, boolean hasGeneratedKeys
    ) throws SQLException;
}
