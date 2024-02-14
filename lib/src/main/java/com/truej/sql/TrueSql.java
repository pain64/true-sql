package com.truej.sql;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

// + TODO: multiple database mapping
// TODO: custom types mapping
//      read from result set
//          SqlTypeName -> (Class<T>, Reader<T>)
//      write to query args
//          Class<T> -> Writer<T>
// + TODO: exception mapper
// + TODO: transactions
// + TODO: flyway - multiple database migration

//final PreparedStatement statement = connection.prepareStatement(
//    "SELECT my_column FROM my_table " +
//        "where search_column IN (SELECT * FROM unnest(\{data}))"
//);
//final String[] values = getValues();
//statement.setArray(1, connection.createArrayOf("text", values));
//final ResultSet rs = statement.executeQuery();
//try {
//    while(rs.next()) {
//    // do some...
//    }
//    } finally {
//    rs.close();
//}

public class TrueSql {
    // FIXME: return type of processor
    public static final StringTemplate.Processor<String, RuntimeException> SQL = StringTemplate::interpolate;

    //public static class SqlProcessor implements StringTemplate
    public static <T> Class<T> g(Class<T> cl) {
        throw new IllegalStateException("this invocation must be erased at compile time");
    }

    public interface ExceptionMapper {
        Exception map(SQLException sqlEx);
    }

    final ExceptionMapper exMapper;
    final DataSource ds;

    public TrueSql(DataSource ds) {
        this.ds = ds;
        this.exMapper = e -> e;
    }

    public TrueSql(DataSource ds, ExceptionMapper exMapper) {
        this.ds = ds;
        this.exMapper = exMapper;
    }

    public final <T> T exec(Class<T> toClass, @Language("sql") String stmt) {
        // insert   returning id;
        // insert
        // begin
        //    insert
        //    insert
        // end
        Runnable r = () -> {
        };

        return null;
    }

    // FIXME: rows affected
    public final <T, U> List<T> execBatched(Class<T> toClass, List<U> rows, Function<U, String> batchQuery) {
        return null;
    }

    public final <T> T[] queryArray(Class<T> toClass, @Language("sql") String query) {
        return null;
    }

    public final <T> List<T> queryList(Class<T> toClass, @Language("sql") String query) {
        return null;
    }

    public final <T> Stream<T> queryStream(Class<T> toClass, @Language("sql") String query) {
        return null;
    }

    public final <T> T queryOne(Class<T> toClass, @Language("sql") String query) {
        return null;
    }

    @Nullable public final <T> T queryOneOrNull(Class<T> toClass, @Language("sql") String query) {
        return null;
    }

    public interface Action<T, D> {
        T run(D database);
    }

    public static <D extends TrueSql, T> T inConnection(D db, Action<T, D> action) {
        return null;
    }

    public static <D extends TrueSql, T> T inTransaction(D db, Action<T, D> action) {
        return null;
    }
}
