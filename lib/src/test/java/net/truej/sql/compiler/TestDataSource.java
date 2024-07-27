package net.truej.sql.compiler;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
            var sql = new String(
                TestDataSource.class.getResourceAsStream(
                    "/schema/" + databaseName + ".create.sql"
                ).readAllBytes()
            );

            for (var part : sql.split("---"))
                connection.createStatement().execute(part);

        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public Connection getConnection() throws SQLException {

        for (var cl : List.of(MainDataSource.class, MainConnection.class)) {
            System.setProperty("truesql." + cl.getName() + ".url", jdbcUrl);
            System.setProperty("truesql." + cl.getName() + ".username", username);
            System.setProperty("truesql." + cl.getName() + ".password", password);
        }

        var connection =  DriverManager.getConnection(jdbcUrl, username, password);
        try {
            connection.createStatement().execute(
                new String(
                    TestDataSource.class.getResourceAsStream(
                        "/schema/" + databaseName + ".cleanup.sql"
                    ).readAllBytes()
                ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return connection;
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
