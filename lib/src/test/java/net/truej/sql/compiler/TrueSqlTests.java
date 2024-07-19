package net.truej.sql.compiler;

import net.truej.sql.util.TestCompiler2;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.extension.*;
import org.postgresql.ds.PGSimpleDataSource;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class TrueSqlTests implements TestInstanceFactory, ParameterResolver {

    @Override public Object createTestInstance(
        TestInstanceFactoryContext factoryContext,
        ExtensionContext extensionContext
    ) throws TestInstantiationException {
        //hsqldb start
        try {
            new JDBCDataSource() {{
                // "jdbc:postgresql://localhost:5432/uikit_sample", "uikit", "1234"
                setURL("jdbc:hsqldb:mem:db");
                setUser("SA");
                setPassword("");

                try (var initConn = this.getConnection()) {
                    var sql = new String(
                        this.getClass().getResourceAsStream("/schema/hsqldb.sql").readAllBytes()
                    );

                    for (var part : sql.split("---"))
                        initConn.createStatement().execute(part);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }};

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //pg start
        try {
            new PGSimpleDataSource() {{
//                 create database truesqldb;
//                 create user sa with password '1234';
//                 grant all privileges on database truesqldb to sa;
//                 \connect truesqldb;
//                 alter schema public owner to sa;

                // "jdbc:postgresql://localhost:5432/uikit_sample", "uikit", "1234"
                setURL("jdbc:postgresql://localhost:5432/truesqldb");
                setUser("sa");
                setPassword("1234");

                try (var initConn = this.getConnection()) {
                    var sql = new String(
                        this.getClass().getResourceAsStream("/schema/postgresql.sql").readAllBytes()
                    );

                    for (var part : sql.split("---"))
                        initConn.createStatement().execute(part);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }};

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        var className = factoryContext.getTestClass().getName();
        var simpleClassName = factoryContext.getTestClass().getSimpleName();
        var classFile = className.replace(".", "/");

        try {
            var uri = new URI(
                STR."file://\{System.getProperty("user.dir")}/src/test/java/" +
                className.replace(".", "/") + "_.java"
            );
            var code = Files.readString(
                Paths.get(
                    STR."\{System.getProperty("user.dir")}/src/test/java/\{classFile}.java"
                )
            );

            var compilationUnits = List.of(
                new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
                    @Override public CharSequence getCharContent(
                        boolean ignoreEncodingErrors
                    ) {
                        return code.replace(
                            "class " + simpleClassName,
                            "class " + simpleClassName + "_ extends " + className
                        );
                    }
                }
            );

            var compiled = TestCompiler2.compile(compilationUnits, List.of());

            //var bytes = r.data.toByteArray();

            var theClass = new URLClassLoader(
                new URL[]{}, this.getClass().getClassLoader()
            ) {{
                compiled.forEach((compClassName, r) -> {
                    var bytes = r.data.toByteArray();
                    defineClass(compClassName, bytes, 0, bytes.length);
                });
            }}.loadClass(className + "_");


            var instance = theClass.newInstance();
            return instance;

        } catch (IOException | URISyntaxException | InstantiationException |
                 IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public boolean supportsParameter(
        ParameterContext parameterContext, ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        var pType = parameterContext.getParameter().getType();
        return pType == MainConnection.class || pType == MainDataSource.class || pType == PgDataSource.class;
    }

    @Override public Object resolveParameter(
        ParameterContext parameterContext, ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        try {
            var hsqlDs = new JDBCDataSource() {{
                // "jdbc:postgresql://localhost:5432/uikit_sample", "uikit", "1234"
                setURL("jdbc:hsqldb:mem:db");
                setUser("SA");
                setPassword("");
            }};

            var pgDs = new PGSimpleDataSource() {{
                setURL("jdbc:postgresql://localhost:5432/truesqldb");
                setUser("sa");
                setPassword("1234");
            }};

            var pType = parameterContext.getParameter().getType();
            if (pType == MainConnection.class) {
                return new MainConnection(hsqlDs.getConnection());
            } else if (pType == MainDataSource.class) {
                return new MainDataSource(hsqlDs);
            } else if (pType == PgDataSource.class) {
                return new PgDataSource(pgDs);
            }

            throw new IllegalStateException("unreachable");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
