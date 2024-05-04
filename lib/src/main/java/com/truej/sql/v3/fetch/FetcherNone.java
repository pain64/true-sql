package com.truej.sql.v3.fetch;

import com.truej.sql.v3.prepare.ManagedAction;
import com.truej.sql.v3.source.RuntimeConfig;

import java.sql.PreparedStatement;

public final class FetcherNone<R> implements
    ManagedAction<PreparedStatement, R, Void> {

    @Override public boolean willStatementBeMoved() { return false; }

    @Override public Void apply(
        RuntimeConfig conf, R executionResult, PreparedStatement stmt, boolean hasGeneratedKeys
    ) {
        return null;
    }
}
