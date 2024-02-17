package com.truej.sql.v3;

import com.truej.sql.v3.prepare.Call;
import com.truej.sql.v3.prepare.Statement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class TrueJdbc {

    public interface WithConnectionAction<T, E extends Exception> {
        T run(Connection cn) throws E;
    }

    public static <T, E extends Exception> T withConnection(
        DataSource ds, WithConnectionAction<T, E> action
    ) throws E {
        try (var cn = ds.getConnection()) {
            return action.run(cn);
        }
    }

    public interface InTransactionAction<T, E extends Exception> {
        T run(Connection cn) throws E;
    }

    public static <T, E extends Exception> T inTransaction(
        Connection connection, InTransactionAction<T, E> action
    ) throws E {
        // TODO
        return action.run(connection);
    }

    public static <T, E extends Exception> T inTransaction(
        DataSource ds, InTransactionAction<T, E> action
    ) throws E {
        return withConnection(ds, conn -> inTransaction(conn, action));
    }

    @FunctionalInterface
    public interface StatementSupplier<T> {
        Statement supply(T element);
    }

    @FunctionalInterface
    public interface CallSupplier<T> {
        Call supply(T element);
    }

    public static class CallParameters {
        public static Void out(String parameterName) {
            return null;
        }

        public static <T> T inout(String parameterName, T value) {
            return value;
        }
    }

    public static <T> Statement batchStmt(T[] data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> Statement batchStmt(List<T> data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> Statement batchStmt(Stream<T> data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> Call batchCall(T[] data, CallSupplier<T> query) {
        return null;
    }

    public static <T> Call batchCall(List<T> data, CallSupplier<T> query) {
        return null;
    }

    public static <T> Call batchCall(Stream<T> data, CallSupplier<T> query) {
        return null;
    }

    public interface ResultSetMapper<T> {
        Iterator<T> map(ResultSet rs);
    }

    public static <T> ResultSetMapper<T> m(Class<T> aClass) {
        throw new RuntimeException("unexpected");
    }
    public static <T> ResultSetMapper<T> g(Class<T> aClass) {
        throw new RuntimeException("unexpected");
    }

    public static class StmtProcessor implements StringTemplate.Processor<Statement, RuntimeException> {
        @Override public Statement process(StringTemplate stringTemplate) throws RuntimeException {
            return null; // TODO: throw
        }
    }

    public static class CallProcessor implements StringTemplate.Processor<com.truej.sql.v3.prepare.Call, RuntimeException> {
        @Override public Call process(StringTemplate stringTemplate) throws RuntimeException {
            return null; // TODO: throw
        }
    }

    public static final StringTemplate.Processor<Statement, RuntimeException> Stmt = new StmtProcessor();
    public static final StringTemplate.Processor<Call, RuntimeException> Call = new CallProcessor();
}
