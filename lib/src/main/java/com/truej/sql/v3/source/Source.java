package com.truej.sql.v3.source;

import com.truej.sql.v3.TrueSql;
import com.truej.sql.v3.prepare.Batchable;
import com.truej.sql.v3.prepare.Statement;

import java.util.List;
import java.util.stream.Stream;

public sealed interface Source
    extends RuntimeConfig, StringTemplate.Processor<Statement, RuntimeException>
    permits DataSourceW, ConnectionW {


    // FIXME: remove???
    interface BatchSupplier<T> {
        class BatchParameters {}

        StringTemplate.Processor<BatchParameters, RuntimeException> B = stringTemplate -> null;

        BatchParameters supply(T element);
    }

    default <T> Statement batch(List<T> data, BatchSupplier<T> query) {
        return null;
    }

    default <T> Statement batch(Stream<T> data, BatchSupplier<T> query) {
        return null;
    }

    @Override default Statement process(StringTemplate stringTemplate) throws RuntimeException {
        return null; // TODO: throw
    }
}