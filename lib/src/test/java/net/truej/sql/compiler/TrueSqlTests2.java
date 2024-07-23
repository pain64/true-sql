package net.truej.sql.compiler;

import net.truej.sql.Bench;
import net.truej.sql.util.TestCompiler2;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.extension.*;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrueSqlTests2 implements
    TestTemplateInvocationContextProvider,
    TestInstanceFactory {

    public enum Database {HSQLDB, POSTGRESQL /*, MYSQL, MARIADB, MSSQL, ORACLE, DB2 */}

    interface DatabaseInstance {
        DataSource getDataSource();
    }

    static void runInitScript(
        DataSource ds, String fileName, String url, String username, String password
    ) {

        for (var cl : List.of(MainDataSource.class, MainConnection.class)) {
            System.setProperty("truesql." + cl.getName() + ".url", url);
            System.setProperty("truesql." + cl.getName() + ".username", username);
            System.setProperty("truesql." + cl.getName() + ".password", password);
        }

        try (var initConn = ds.getConnection()) {
            var sql = new String(
                TrueSqlTests2.class.getResourceAsStream(fileName).readAllBytes()
            );

            for (var part : sql.split("---"))
                initConn.createStatement().execute(part);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Database, DatabaseInstance> instances;
    static {
        var pgContainer = new PostgreSQLContainer<>("postgres:16.3");

        pgContainer.start();

        instances = Map.of(
            Database.HSQLDB, new DatabaseInstance() {
                @Override public DataSource getDataSource() {
                    return new JDBCDataSource() {{
                        setURL("jdbc:hsqldb:mem:test");
                        setUser("SA");
                        setPassword("");
                        runInitScript(this, "/schema/hsqldb.sql", url, user, password);
                    }};
                }
            },
            Database.POSTGRESQL, new DatabaseInstance() {
                @Override public DataSource getDataSource() {
                    return new PGSimpleDataSource() {{
                        setURL(pgContainer.getJdbcUrl());
                        setUser(pgContainer.getUsername());
                        setPassword(pgContainer.getPassword());
                        runInitScript(
                            this, "/schema/postgresql.sql", pgContainer.getJdbcUrl(),
                            pgContainer.getUsername(), pgContainer.getPassword()
                        );
                    }};
                }
            }
        );
    }

    @Retention(RetentionPolicy.RUNTIME) public @interface DisabledOn {
        Database[] value() default {};
    }

    @Retention(RetentionPolicy.RUNTIME) public @interface EnableOn {
        Database[] value() default {};
    }

    @Repeatable(Messages.class)
    @Retention(RetentionPolicy.RUNTIME) public @interface Message {
        Diagnostic.Kind kind();
        String text();
    }

    @Retention(RetentionPolicy.RUNTIME) public @interface Messages {
        Message[] value() default {};
    }

    @Override public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    @Override public Object createTestInstance(
        TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext
    ) throws TestInstantiationException {

        var classPackage = factoryContext.getTestClass().getPackage().getName();
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

            var expectedMessages = Arrays.stream(
                    factoryContext.getTestClass().getAnnotationsByType(Message.class)
                )
                .map(m -> new TestCompiler2.Message(m.kind(), m.text())).toList();

            var expectCompilationError = expectedMessages
                .stream().anyMatch(m -> m.kind() == Diagnostic.Kind.ERROR);

            var compiled = TestCompiler2.compile(compilationUnits, expectedMessages);

            var forDefine = expectCompilationError ?
                TestCompiler2.compile(List.of( // empty test class stub
                    new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
                        @Override public CharSequence getCharContent(
                            boolean ignoreEncodingErrors
                        ) {
                            var methods = Arrays.stream(factoryContext.getTestClass().getDeclaredMethods())
                                .filter(m -> m.getName().equals("test"))
                                .map(mt -> {
                                    var parameters = Arrays.stream(mt.getParameters())
                                        .map(p -> p.getType().getName() + " " + p.getName())
                                        .collect(Collectors.joining(","));

                                    return "@TestTemplate public void " + mt.getName() + "(" + parameters + "){}";
                                })
                                .collect(Collectors.joining("\n"));

                            return
                                "package " + classPackage + ";\n" +
                                "import org.junit.jupiter.api.TestTemplate;\n" +
                                "public class " + simpleClassName + "_ extends " + className + "{" +
                                methods + "}";
                        }
                    }
                ), List.of()) : compiled;

            var theClass = new URLClassLoader(
                new URL[]{}, this.getClass().getClassLoader()
            ) {{
                forDefine.forEach((compClassName, r) -> {
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

    @Override public Stream<TestTemplateInvocationContext>
    provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        return Arrays.stream(Database.values())
            .filter(d -> {
                var tm = extensionContext.getTestMethod().get();
                var enabled = tm.getAnnotation(EnableOn.class);
                var disabled = tm.getAnnotation(DisabledOn.class);

                return
                    (enabled == null || Arrays.asList(enabled.value()).contains(d)) &&
                    (disabled == null || !Arrays.asList(disabled.value()).contains(d));
            })
            .map(this::invocationContext);
    }

    private TestTemplateInvocationContext invocationContext(Database database) {

        var instance = instances.get(database);

        return new TestTemplateInvocationContext() {
            @Override public String getDisplayName(int invocationIndex) {
                instance.getDataSource(); // publish schema for the Compiler
                return database.name().toLowerCase();
            }

            @Override public List<Extension> getAdditionalExtensions() {
                return List.of(new ParameterResolver() {

                    @Override public boolean supportsParameter(
                        ParameterContext parameterContext, ExtensionContext extensionContext
                    ) throws ParameterResolutionException {
                        var pType = parameterContext.getParameter().getType();
                        return pType == MainConnection.class ||
                               pType == MainDataSource.class ||
                               pType == MainDataSourceUnchecked.class;
                    }

                    @Override public Object resolveParameter(
                        ParameterContext parameterCtx, ExtensionContext extensionCtx
                    ) {
                        var pType = parameterCtx.getParameter().getType();
                        if (pType == MainConnection.class) {
                            try {
                                return new MainConnection(
                                    instance.getDataSource().getConnection()
                                );
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (pType == MainDataSource.class)
                            return new MainDataSource(instance.getDataSource());
                        else if (pType == MainDataSourceUnchecked.class)
                            return new MainDataSourceUnchecked(instance.getDataSource());

                        throw new IllegalStateException("unreachable");
                    }
                });
            }
        };
    }
}
