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
//                    // TODO: create test fixture: t1, p1, f1
//                    initConn.createStatement().execute("""
//                            create table if not exists t1(id bigint primary key, v varchar(64));
//                            """);
////                    initConn.createStatement().execute("""
////                        alter table t1 add constraint t1_pk primary key (id);
////                        """);
//
//                    initConn.createStatement().execute("""
//                            delete from t1;
//                            """);
//
//                    initConn.createStatement().execute("""
//                            insert into t1(id, v) values(1, 'a');
//                            insert into t1(id, v) values(2, 'b');
//                            """);
//                    initConn.createStatement().execute("""
//                            drop procedure p1 if exists;
//                            create procedure p1(in x int, inout y int, out z int)
//                              begin atomic
//                                 set y = y + x;
//                                 set z = y + x;
//                              end
//                            """);
                    initConn.createStatement().execute("""
                            DROP SCHEMA PUBLIC CASCADE;
                            --CREATE SCHEMA PUBLIC;
                            """);
                    initConn.createStatement().execute("""
                            CREATE TABLE user (
                            	id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                            	name varchar(100) NOT NULL,
                            	info varchar(200)
                            );
                            drop table if exists clinic;
                            CREATE TABLE clinic (
                            	id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                            	name varchar(100) NOT NULL,
                            	city_id bigint NOT NULL
                            );
                            
                            CREATE TABLE city (
                            	id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                            	name varchar(50) NOT NULL
                            );
                            CREATE TABLE bill (
                            	id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                            	amount decimal(15,2) NOT NULL,
                            	discount decimal(15,2),
                            	date datetime NOT NULL
                            );
                            CREATE TABLE clinic_users (
                            	clinic_id bigint NOT NULL,
                            	user_id bigint NOT NULL
                            );
                            CREATE TABLE user_bills (
                            	user_id bigint NOT NULL,
                            	bill_id bigint NOT NULL
                            );
                    """);
                    initConn.createStatement().execute("""
                            ALTER TABLE clinic ADD CONSTRAINT clinic_fk2 FOREIGN KEY (city_id) REFERENCES city(id);
                                        
                            ALTER TABLE clinic_users ADD CONSTRAINT clinic_users_fk0 FOREIGN KEY (clinic_id) REFERENCES clinic(id);
                            
                            ALTER TABLE clinic_users ADD CONSTRAINT clinic_users_fk1 FOREIGN KEY (user_id) REFERENCES user(id);
                            ALTER TABLE user_bills ADD CONSTRAINT user_bills_fk0 FOREIGN KEY (user_id) REFERENCES user(id);
                            
                            ALTER TABLE user_bills ADD CONSTRAINT user_bills_fk1 FOREIGN KEY (bill_id) REFERENCES bill(id);
                    """);
                    initConn.createStatement().execute("""
                            insert into user(id, name, info) values(1, 'Joe', null);
                            insert into user(id, name, info) values(2, 'Donald', 'Do not disturb');
                            
                            insert into city(id, name) values(1, 'London');
                            insert into city(id, name) values(2, 'Paris');
                            
                            insert into clinic(id, name, city_id) values(1, 'Paris Neurology Hospital', 2);
                            insert into clinic(id, name, city_id) values(2, 'London Heart Hospital', 1);
                            insert into clinic(id, name, city_id) values(3, 'Diagnostic center', 1);
                            
                            insert into bill(id, amount, discount, date) values(1, 2000.55, null, cast('2024-07-01 12:00:00' as datetime));
                            insert into bill(id, amount, discount, date) values(2, 1000.20, null, cast('2024-07-01 16:00:00' as datetime));
                            insert into bill(id, amount, discount, date) values(3, 5000, null, cast('2024-08-01 15:00:00' as datetime));
                            insert into bill(id, amount, discount, date) values(4, 7000.77, null, cast('2024-08-01 15:00:00' as datetime));
                            insert into bill(id, amount, discount, date) values(5, 500.10, null, cast('2024-09-01 15:00:00' as datetime));
                    """);
                    initConn.createStatement().execute("""
                            create procedure digit_magic(in x int, inout y int, out z int)
                              begin atomic
                                 set y = y + x;
                                 set z = y + x;
                              end;
                            create procedure bill_zero()
                            modifies sql data
                              begin atomic
                                 update bill set amount = 0;
                              end
                            create procedure discount_bill(in datedisc datetime)
                            modifies sql data
                              begin atomic
                                 update bill set discount = amount * 0.1 where date = datedisc;
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
