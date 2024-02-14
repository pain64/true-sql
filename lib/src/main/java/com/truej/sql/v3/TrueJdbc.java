package com.truej.sql.v3;

import com.truej.sql.v3.prepare.Call;
import com.truej.sql.v3.prepare.Statement;

import javax.sql.DataSource;
import java.sql.Connection;

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
        T run() throws E;
    }

    public static <T, E extends Exception> T inTransaction(
        Connection connection, WithConnectionAction<T, E> action
    ) throws E {
        // TODO
        return action.run(connection);
    }

    public static <T, E extends Exception> T inTransaction(
        DataSource ds, WithConnectionAction<T, E> action
    ) throws E {
        return withConnection(ds, conn -> inTransaction(conn, action));
    }


    public static <T> Class<T> m(Class<T> aClass) {
        throw new RuntimeException("unexpected");
    }
    public static <T> Class<T> g(Class<T> aClass) {
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
