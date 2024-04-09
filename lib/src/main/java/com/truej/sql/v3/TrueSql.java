package com.truej.sql.v3;

import com.truej.sql.v3.fetch.ResultSetMapper;
import com.truej.sql.v3.prepare.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Stream;

public class TrueSql {
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    public @interface Process {}

    // FIXME: remove???
    public interface BatchSupplier<T, B extends Batchable> {
        B supply(T element);
    }

    public static class CallParameters {
        public static Void out(String parameterName) {
            return null;
        }

        public static <T> T inout(String parameterName, T value) {
            return value;
        }
    }

    public static <T, B extends Batchable> B batch(List<T> data, BatchSupplier<T, B> query) {
        return null;
    }

    public static <T, B extends Batchable> B batch(Stream<T> data, BatchSupplier<T, B> query) {
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
