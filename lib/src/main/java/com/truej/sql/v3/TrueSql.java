package com.truej.sql.v3;

import com.truej.sql.v3.fetch.ResultSetMapper;
import com.truej.sql.v3.fetch.UpdateResult;
import com.truej.sql.v3.prepare.BatchCall;
import com.truej.sql.v3.prepare.BatchStatement;
import com.truej.sql.v3.prepare.Call;
import com.truej.sql.v3.prepare.Statement;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

public class TrueSql {

    public interface WithConnectionAction<T, E extends Exception> {
        T run(Connection cn) throws E;
    }

    public static <T, E extends Exception> T withConnection(
        DataSource ds, WithConnectionAction<T, E> action
    ) throws E {
        try (var cn = ds.getConnection()) {
            return action.run(cn);
        } catch (SQLException e) {
            throw new SqlExceptionR(e);
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
    }

    public static <T> BatchStatement batchStmt(T[] data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> BatchStatement batchStmt(List<T> data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> BatchStatement batchStmt(Stream<T> data, StatementSupplier<T> query) {
        return null;
    }

    public static <T> BatchCall batchCall(T[] data, CallSupplier<T> query) {
        return null;
    }

    public static <T> BatchCall batchCall(List<T> data, CallSupplier<T> query) {
        return null;
    }

    public static <T> BatchCall batchCall(Stream<T> data, CallSupplier<T> query) {
        return null;
    }

    public static <T> Class<UpdateResult<T>> updateCount(Class<T> aClass) {
        return null;
    }

    public static <T, H> ResultSetMapper<T, H> m(Class<T> aClass) {
        throw new RuntimeException("unexpected");
    }

    public static <T, H> ResultSetMapper<T, H> m(Class<T> aClass, H hints) {
        throw new RuntimeException("unexpected");
    }

    public static <T, H> ResultSetMapper<T, H> g(Class<T> aClass) {
        throw new RuntimeException("unexpected");
    }

    public static <T, H> ResultSetMapper<T, H> g(Class<T> aClass, H hints) {
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
