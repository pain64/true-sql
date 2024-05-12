package com.truej.sql.v3.prepare;

import com.truej.sql.v3.source.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Base<S, P extends PreparedStatement, R, U> extends FetchApi<P, R, U> {

    // Generated code API
    protected abstract Source source();
    protected abstract P prepare(Connection connection) throws SQLException;
    protected void afterPrepare(PreparedStatement stmt) throws SQLException {}
    protected boolean isAsGeneratedKeys() { return false; }
    protected void bindArgs(P stmt) throws SQLException {}
    // End

    public interface StatementConfigurator {
        void configure(PreparedStatement statement) throws SQLException;
    }

    abstract R execute(P stmt) throws SQLException;
    abstract U getUpdateCount(R executionResult, P stmt) throws SQLException;

    public S afterPrepare(StatementConfigurator config) {
        throw new RuntimeException("compile-time implemented!");
    }

    public FetchApi<P, R, U> asCall() {
        throw new RuntimeException("compile-time implemented!");
    }

    public FetchApi<P, R, U> asGeneratedKeys(int... columnNumbers) {
        throw new RuntimeException("compile-time implemented!");
    }

    public FetchApi<P, R, U> asGeneratedKeys(String... columnNames) {
        throw new RuntimeException("compile-time implemented!");
    }
}