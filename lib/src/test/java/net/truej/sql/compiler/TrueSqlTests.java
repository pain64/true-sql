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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrueSqlTests implements
    TestTemplateInvocationContextProvider, TestInstanceFactory, ExecutionCondition {

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

    private static final Map<Database, TestDataSource> instances;
    static {

        var pgContainer = new PostgreSQLContainer<>("postgres:16.3")
            .withReuse(true);
        var mysqlContainer = new MySQLContainer<>("mysql:9.0.1")
            .withUsername("root")
            .withReuse(true);
        var mariaDbContainer = new MariaDBContainer<>("mariadb:11.4.2-ubi9")
            .withReuse(true);
        var mssqlContainer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense()
            .withReuse(true);
        // jdemovic1983/oracle23ai
        //var oracleContainer = new OracleContainer("gvenzl/oracle-xe:latest")
        var dockerImageName = DockerImageName.parse("gvenzl/oracle-free:23-slim")
            .asCompatibleSubstituteFor("gvenzl/oracle-xe");

        var oracleContainer = new OracleContainer(dockerImageName)
            .withDatabaseName("test")
            .withUsername("testUser")
            .withPassword("testPassword")
            .withReuse(true);

//        var oracleContainer = new OracleContainer("jdemovic1983/oracle23ai:v1")
//            .withDatabaseName("test")
//            .withUsername("testUser")
//            .withPassword("testPassword")
//            .withReuse(true);

        pgContainer.start();
        mysqlContainer.start();
        mariaDbContainer.start();
        mssqlContainer.start();
        oracleContainer.start();

        instances = Map.of(
            Database.HSQLDB, new TestDataSource("jdbc:hsqldb:mem:test", "SA", "", "hsqldb") // +
            ,
            Database.POSTGRESQL, new TestDataSource( // +
                pgContainer.getJdbcUrl(),
                pgContainer.getUsername(),
                pgContainer.getPassword(),
                "postgresql"
            )
            ,
            Database.MYSQL, new TestDataSource(
                mysqlContainer.getJdbcUrl() + "?allowMultiQueries=true",
                mysqlContainer.getUsername(),
                mysqlContainer.getPassword(),
                "mysql"
            )
            ,
            Database.MARIADB, new TestDataSource( // +
                mariaDbContainer.getJdbcUrl() + "?allowMultiQueries=true",
                mariaDbContainer.getUsername(),
                mariaDbContainer.getPassword(),
                "mariadb"
            )
            ,
            Database.MSSQL, new TestDataSource( // +?
                mssqlContainer.getJdbcUrl() + ";encrypt=false;TRUSTED_CONNECTION=TRUE",
                mssqlContainer.getUsername(),
                mssqlContainer.getPassword(),
                "mssql"
            )
            ,
            Database.ORACLE, new TestDataSource(
                oracleContainer.getJdbcUrl(),
                oracleContainer.getUsername(),
                oracleContainer.getPassword(),
                "oracle"
            )
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
                    (enabled == null || Arrays.asList(enabled.value()).contains(d)) &&
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

    @Override public Object createTestInstance(
        TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext
    ) throws TestInstantiationException {

        var classPackage = factoryContext.getTestClass().getPackage().getName();
        var className = factoryContext.getTestClass().getName();
        var simpleClassName = factoryContext.getTestClass().getSimpleName();
        var classFile = className.replace(".", "/");

        var mutableEnv = (Map<String, String>) null;
        try {
            var env = System.getenv();
            var field = env.getClass().getDeclaredField("m");
            field.setAccessible(true);
            //noinspection unchecked
            mutableEnv = (Map<String, String>) field.get(env);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        var envToSet = factoryContext.getTestClass().getAnnotation(Env.class);

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
                            "class " + simpleClassName,
                            "class " + simpleClassName + "_ extends " + className
                        ).replace(
                            simpleClassName + "G.*",
                            simpleClassName + "_G.*"
                        );
                    }
                }
            );

            var expectedMessages = Arrays.stream(
                    factoryContext.getTestClass().getAnnotationsByType(Message.class)
                )
                .map(m -> new TestCompiler.Message(m.kind(), m.text())).toList();

            var containsOutput = factoryContext.getTestClass().getAnnotation(ContainsOutput.class);

            var expectCompilationError = expectedMessages
                .stream().anyMatch(m -> m.kind() == Diagnostic.Kind.ERROR);

            if (envToSet != null)
                mutableEnv.put(envToSet.key(), envToSet.value());

            var compiled = TestCompiler.compile(
                compilationUnits, expectedMessages,
                containsOutput == null ? null : containsOutput.value()
            );

            var forDefine = expectCompilationError ?
                TestCompiler.compile(List.of( // empty test class stub
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
                ), List.of(), null) : compiled;

            var theClass = new URLClassLoader(
                new URL[]{}, this.getClass().getClassLoader()
            ) {{
                forDefine.forEach((compClassName, r) -> {
                    var bytes = r.data.toByteArray();
                    defineClass(compClassName, bytes, 0, bytes.length);
                });
            }}.loadClass(className + "_");

            return theClass.newInstance();

        } catch (IOException | URISyntaxException | InstantiationException |
                 IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (envToSet != null) mutableEnv.remove(envToSet.key());
        }
    }

    @Override public Stream<TestTemplateInvocationContext>
    provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        return enabledDatabases(extensionContext).stream()
            .map(this::invocationContext);
    }

    private TestTemplateInvocationContext invocationContext(Database database) {

        var instance = instances.get(database);

        return new TestTemplateInvocationContext() {
            @Override public String getDisplayName(int invocationIndex) {
                instance.publishProperties();
                return database.name().toLowerCase();
            }

            @Override public List<Extension> getAdditionalExtensions() {
                return List.of(new ParameterResolver() {

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
                                    .newInstance(instance.getConnection());
                            else if (DataSourceW.class.isAssignableFrom(pType))
                                return pType.getConstructor(DataSource.class)
                                    .newInstance(instance);

                            throw new IllegalStateException("unreachable");

                        } catch (SQLException | InvocationTargetException |
                                 InstantiationException | IllegalAccessException |
                                 NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
            }
        };
    }
}
