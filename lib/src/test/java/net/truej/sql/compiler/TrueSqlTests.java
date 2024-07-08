package net.truej.sql.compiler;

import net.truej.sql.util.TestCompiler2;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.extension.*;

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

        try {
            new JDBCDataSource() {{
                // "jdbc:postgresql://localhost:5432/uikit_sample", "uikit", "1234"
                setURL("jdbc:hsqldb:mem:db");
                setUser("SA");
                setPassword("");

                try (var initConn = this.getConnection()) {
                    // TODO: create test fixture: t1, p1, f1
                    initConn.createStatement().execute("""
                            create table if not exists t1(id bigint primary key, v varchar(64));
                            """);
//                    initConn.createStatement().execute("""
//                        alter table t1 add constraint t1_pk primary key (id);
//                        """);

                    initConn.createStatement().execute("""
                            delete from t1;
                            """);

                    initConn.createStatement().execute("""
                            insert into t1(id, v) values(1, 'a');
                            insert into t1(id, v) values(2, 'b');
                            """);
                    initConn.createStatement().execute("""
                            drop procedure p1 if exists;
                            create procedure p1(in x int, inout y int, out z int)
                              begin atomic
                                 set y = y + x;
                                 set z = y + x;
                              end
                            """);
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

            var compiled = TestCompiler2.compile(compilationUnits);

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
        return pType == MainConnection.class || pType == MainDataSource.class;
    }

    @Override public Object resolveParameter(
        ParameterContext parameterContext, ExtensionContext extensionContext
    ) throws ParameterResolutionException {
        try {
            var ds = new JDBCDataSource() {{
                // "jdbc:postgresql://localhost:5432/uikit_sample", "uikit", "1234"
                setURL("jdbc:hsqldb:mem:db");
                setUser("SA");
                setPassword("");
            }};

            var pType = parameterContext.getParameter().getType();
            if (pType == MainConnection.class) {
                return new MainConnection(ds.getConnection());
            } else if (pType == MainDataSource.class) {
                return new MainDataSource(ds);
            }

            throw new IllegalStateException("unreachable");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
