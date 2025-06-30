package net.truej.sql.compiler;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public record TestDataSource(
    String jdbcUrl, String username, String password, String databaseName
) implements DataSource {

    public TestDataSource {
//        var rsToString = (Function<ResultSet, String>) rs ->
//            Stream.iterate(
//                rs, t -> {
//                    try {
//                        return t.next();
//                    } catch (SQLException e) {
//                        throw new RuntimeException(e);
//                    }
//                }, t -> t
//            ).map(r -> {
//                try {
//                    var s = "";
//                    for (var i = 0; i < r.getMetaData().getColumnCount(); i++)
//                        s += r.getMetaData().getColumnLabel(i + 1) + "=" + r.getObject(i + 1) + ";";
//                    return s;
//
//                } catch (SQLException e) {
//                    throw new RuntimeException(e);
//                }
//            }).collect(Collectors.joining("\n"));

        try (
            var connection = DriverManager.getConnection(jdbcUrl, username, password)
        ) {
            var isSchemaDefined = true;
            try {
                connection.createStatement().execute("select * from users");
            } catch (SQLException e) {
                isSchemaDefined = false;
            }

            if(!isSchemaDefined) {
                var sql = new String(
                    TestDataSource.class.getResourceAsStream(
                        "/schema/" + databaseName + ".create.sql"
                    ).readAllBytes()
                );

                for (var part : sql.split("---"))
                    connection.createStatement().execute(part);
            }

            var sql = new String(
                TestDataSource.class.getResourceAsStream(
                    "/schema/" + databaseName + ".cleanup.sql"
                ).readAllBytes()
            );

            connection.createStatement().execute(sql);

        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<String> publishProperties() {
        var dest = new ArrayList<String>();

        for (var cl : List.of(
            MainDataSource.class, MainConnection.class, PgConnection.class,
            MssqlConnection.class, OracleConnection.class, MariaDbConnection.class
        )) {
            dest.add("-Atruesql." + cl.getName() + ".url=" + jdbcUrl);
            dest.add("-Atruesql." + cl.getName() + ".username=" + username);
            dest.add("-Atruesql." + cl.getName() + ".password=" + password);
        }

        return dest;
    }

    @Override public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override public Connection getConnection(String username, String password) {
        throw new IllegalStateException("unexpected");
    }

    @Override public PrintWriter getLogWriter() {
        throw new IllegalStateException("unexpected");
    }

    @Override public void setLogWriter(PrintWriter out) {
        throw new IllegalStateException("unexpected");
    }

    @Override public void setLoginTimeout(int seconds) {
        throw new IllegalStateException("unexpected");
    }

    @Override public int getLoginTimeout() {
        throw new IllegalStateException("unexpected");
    }

    @Override public Logger getParentLogger() {
        throw new IllegalStateException("unexpected");
    }

    @Override public <T> T unwrap(Class<T> iface) {
        throw new IllegalStateException("unexpected");
    }

    @Override public boolean isWrapperFor(Class<?> iface) {
        throw new IllegalStateException("unexpected");
    }
}
