package com.truej.sql.util;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class TestDataSource implements DataSource {
    public final Connection wrapped;
    public TestDataSource(Connection wrapped) { this.wrapped = wrapped; }

    @Override public Connection getConnection() throws SQLException {
        return wrapped;
    }
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new RuntimeException("not stubbed");
    }
    @Override public PrintWriter getLogWriter() throws SQLException {
        throw new RuntimeException("not stubbed");
    }
    @Override public void setLogWriter(PrintWriter out) throws SQLException {
        throw new RuntimeException("not stubbed");
    }
    @Override public void setLoginTimeout(int seconds) throws SQLException {
        throw new RuntimeException("not stubbed");
    }
    @Override public int getLoginTimeout() throws SQLException {
        throw new RuntimeException("not stubbed");
    }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new RuntimeException("not stubbed");
    }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new RuntimeException("not stubbed");
    }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new RuntimeException("not stubbed");
    }
}
