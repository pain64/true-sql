package net.truej.sql.compiler;

import net.truej.sql.source.ConnectionW;
import net.truej.sql.source.DataSourceW;
import net.truej.sql.util.TestCompiler;
import org.junit.jupiter.api.extension.*;
import org.testcontainers.containers.*;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrueSqlTests implements
    TestInstanceFactory, TestTemplateInvocationContextProvider, ExecutionCondition {

    public enum Database {HSQLDB, POSTGRESQL, MYSQL, MARIADB, MSSQL, ORACLE /*, DB2 */}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    public @interface DisabledOn {
        Database[] value() default {};
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    public @interface EnableOn {
        Database[] value() default {};
    }

    @Repeatable(Messages.class) @Retention(RetentionPolicy.RUNTIME)
    public @interface Message {
        Diagnostic.Kind kind();
        String text();
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    public @interface Messages {
        Message[] value() default {};
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    public @interface ContainsOutput {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    public @interface Env {
        String key();
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    public @interface PackagePrivateAccessModifier { }

    private static final Map<Database, Future<TestDataSource>> instances;
    private static final ExecutorService executor = Executors.newFixedThreadPool(
        //(int) (Runtime.getRuntime().availableProcessors() * 0.8)
        6
    );

    static {

        var hsqldb = executor.submit(() ->
            new TestDataSource("jdbc:hsqldb:mem:test", "SA", "", "hsqldb")
        );

        var postgresql = executor.submit(() -> {
            var pgContainer = new PostgreSQLContainer<>("postgres:16.3")
                .withReuse(true);
            pgContainer.start();

            return new TestDataSource(
                pgContainer.getJdbcUrl() + "?ssl=false",
                pgContainer.getUsername(),
                pgContainer.getPassword(),
                "postgresql"
            );
        });

        var mysql = executor.submit(() -> {
            var mysqlContainer = new MySQLContainer<>("mysql:9.0.1")
                .withUsername("root")
                .withReuse(true);
            mysqlContainer.start();

            return new TestDataSource(
                mysqlContainer.getJdbcUrl() + "?allowMultiQueries=true&useSSL=false",
                mysqlContainer.getUsername(),
                mysqlContainer.getPassword(),
                "mysql"
            );
        });

        var mariadb = executor.submit(() -> {
            var mariaDbContainer = new MariaDBContainer<>("mariadb:11.4.2-ubi9")
                .withReuse(true);
            mariaDbContainer.start();

            return new TestDataSource(
                mariaDbContainer.getJdbcUrl() + "?allowMultiQueries=true&useSSL=false",
                mariaDbContainer.getUsername(),
                mariaDbContainer.getPassword(),
                "mariadb"
            );
        });

        var mssql = executor.submit(() -> {
            var mssqlContainer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                .acceptLicense()
                .withReuse(true);
            mssqlContainer.start();

            return new TestDataSource(
                mssqlContainer.getJdbcUrl() + ";encrypt=false;TRUSTED_CONNECTION=TRUE",
                mssqlContainer.getUsername(),
                mssqlContainer.getPassword(),
                "mssql"
            );
        });

        var oracle = executor.submit(() -> {
            var oracleContainer = new OracleContainer(
                DockerImageName.parse("gvenzl/oracle-free:23-slim")
                    .asCompatibleSubstituteFor("gvenzl/oracle-xe")
            )
                .withDatabaseName("test")
                .withUsername("testUser")
                .withPassword("testPassword")
                .withReuse(true);

            oracleContainer.start();

            return new TestDataSource(
                oracleContainer.getJdbcUrl(),
                oracleContainer.getUsername(),
                oracleContainer.getPassword(),
                "oracle"
            );
        });

        instances = Map.of(
            Database.HSQLDB, hsqldb
            ,
            Database.POSTGRESQL, postgresql
            ,
            Database.MYSQL, mysql
            ,
            Database.MARIADB, mariadb
            ,
            Database.MSSQL, mssql
            ,
            Database.ORACLE, oracle
        );
    }

    List<Database> enabledDatabases(ExtensionContext extensionContext) {
        return Arrays.stream(Database.values())
            .filter(d -> {
                var tm = extensionContext.getTestClass().get();
                var enabled = tm.getAnnotation(EnableOn.class);
                var disabled = tm.getAnnotation(DisabledOn.class);

                return
                    instances.containsKey(d) &&
                    (enabled == null || Arrays.asList(enabled.value()).contains(d)) && // ???
                    (disabled == null || !Arrays.asList(disabled.value()).contains(d));
            }).toList();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return !enabledDatabases(context).isEmpty()
            ? ConditionEvaluationResult.enabled("has at least one db to run")
            : ConditionEvaluationResult.disabled("has no db to run");
    }

    @Override public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    record Parametrized(Database onDatabase, Future<Class<?>> compileTask) { }
    Map<ExtensionContext, Parametrized> running = new ConcurrentHashMap<>();

    @Override public Object createTestInstance(
        TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext
    ) throws TestInstantiationException {

        try {
            var cons = running.get(extensionContext).compileTask.get().getDeclaredConstructors();
            cons[0].setAccessible(true);
            return cons[0].newInstance();
        } catch (InstantiationException | IllegalAccessException | InterruptedException |
                 ExecutionException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public Stream<TestTemplateInvocationContext>
    provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {

        var enabledAll = enabledDatabases(extensionContext);
        var testClass = extensionContext.getTestClass().get();

        var futures = enabledAll.stream().map(
            db -> new Parametrized(db, executor.submit(() -> {
                final List<String> extraArgs;
                try {
                    extraArgs = instances.get(db).get().publishProperties();
                    var envToSet = testClass.getAnnotation(Env.class);
                    if (envToSet != null)
                        extraArgs.add("-A" + envToSet.key() + "=" + envToSet.value());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

                var classPackage = testClass.getPackage().getName();
                var className = testClass.getName();
                var simpleClassName = testClass.getSimpleName();
                var classFile = className.replace(".", "/");

                try {
                    var uri = new URI(
                        "file://" + System.getProperty("user.dir") + "/src/test/java/" +
                        className.replace(".", "/") + "_.java"
                    );

                    var code = Files.readString(
                        Paths.get(
                            System.getProperty("user.dir") + "/src/test/java/" + classFile + ".java"
                        )
                    );

                    var compilationUnits = List.of(
                        new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
                            @Override public CharSequence getCharContent(
                                boolean ignoreEncodingErrors
                            ) {
                                return code.replace(
                                    (testClass
                                         .getAnnotation(PackagePrivateAccessModifier.class) == null
                                        ? "class " : "public class "
                                    ) + simpleClassName,
                                    "class " + simpleClassName + "_ extends " + className
                                ).replace(
                                    simpleClassName + "G.*",
                                    simpleClassName + "_G.*"
                                );
                            }
                        }
                    );

                    var expectedMessages = Arrays.stream(
                            testClass.getAnnotationsByType(Message.class)
                        )
                        .map(m -> new TestCompiler.Message(m.kind(), m.text())).toList();

                    var containsOutput = testClass.getAnnotation(ContainsOutput.class);

                    var expectCompilationError = expectedMessages
                        .stream().anyMatch(m -> m.kind() == Diagnostic.Kind.ERROR);

                    var compiled = TestCompiler.compile(
                        compilationUnits, extraArgs, expectedMessages,
                        containsOutput == null ? null : containsOutput.value()
                    );

                    if (enabledAll.indexOf(db) != 0)
                        return null;

                    var forDefine = expectCompilationError ?
                        TestCompiler.compile(List.of( // empty test class stub
                            new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
                                @Override public CharSequence getCharContent(
                                    boolean ignoreEncodingErrors
                                ) {
                                    var methods = Arrays.stream(testClass.getDeclaredMethods())
                                        .filter(m -> m.getName().equals("test"))
                                        .map(mt -> {
                                            var parameters = Arrays.stream(mt.getParameters())
                                                .map(p -> p.getType().getName() + " " + p.getName())
                                                .collect(Collectors.joining(","));

                                            return "public void " + mt.getName() + "(" + parameters + "){}";
                                        })
                                        .collect(Collectors.joining("\n"));

                                    return
                                        "package " + classPackage + ";\n" +
                                        "public class " + simpleClassName + "_ extends " + className + "{" +
                                        methods + "}";
                                }
                            }
                        ), extraArgs, List.of(), null) : compiled;

                    return new URLClassLoader(
                        new URL[]{}, this.getClass().getClassLoader()
                    ) {{
                        forDefine.forEach((compClassName, r) -> {
                            var bytes = r.data.toByteArray();
                            defineClass(compClassName, bytes, 0, bytes.length);
                        });
                    }}.loadClass(className + "_");

                } catch (IOException | URISyntaxException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }))
        ).toList();

        running.put(extensionContext.getParent().get(), futures.get(0));
        return futures.stream().map(this::invocationContext);
    }

    private TestTemplateInvocationContext invocationContext(Parametrized parametrized) {

        var instance = instances.get(parametrized.onDatabase);

        return new TestTemplateInvocationContext() {
            @Override public String getDisplayName(int invocationIndex) {
                return parametrized.onDatabase.name().toLowerCase();
            }

            @Override public List<Extension> getAdditionalExtensions() {
                return List.of(
                    (BeforeEachCallback) context -> {
                        try {
                            // check that compilation succeeded
                            parametrized.compileTask.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    new ParameterResolver() {
                        @Override public boolean supportsParameter(
                            ParameterContext parameterContext, ExtensionContext extensionContext
                        ) throws ParameterResolutionException {
                            var pType = parameterContext.getParameter().getType();
                            return ConnectionW.class.isAssignableFrom(pType) ||
                                   DataSourceW.class.isAssignableFrom(pType);
                        }

                        @Override public Object resolveParameter(
                            ParameterContext parameterCtx, ExtensionContext extensionCtx
                        ) {
                            try {
                                var pType = parameterCtx.getParameter().getType();

                                if (ConnectionW.class.isAssignableFrom(pType))
                                    return pType.getConstructor(Connection.class)
                                        .newInstance(instance.get().getConnection());
                                else if (DataSourceW.class.isAssignableFrom(pType))
                                    return pType.getConstructor(DataSource.class)
                                        .newInstance(instance.get());

                                throw new IllegalStateException("unreachable");

                            } catch (SQLException | InvocationTargetException |
                                     InstantiationException |
                                     IllegalAccessException | NoSuchMethodException |
                                     ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                );
            }
        };
    }
}
