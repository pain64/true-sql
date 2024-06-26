package com.truej.sql.compiler;

import com.truej.sql.util.TestCompiler2;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TrueSqlTests implements TestInstanceFactory {

    @Override public Object createTestInstance(
        TestInstanceFactoryContext factoryContext,
        ExtensionContext extensionContext
    ) throws TestInstantiationException {

        var className = factoryContext.getTestClass().getName();
        var classFile = className.replace(".", "/");

        try {
            var uri = new URI(
                STR."file://\{System.getProperty("user.dir")}/src/test/java/com/truej/sql/compiler/A.java"
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
                    )  {
                        return code.replace("class SuperTest", "class A extends com.truej.sql.compiler.SuperTest");
                    }
                }
            );

            var r = TestCompiler2.compile(compilationUnits).get("com.truej.sql.compiler.A");

            var bytes = r.data.toByteArray();

            var theClass = new URLClassLoader(
                new URL[]{ }, this.getClass().getClassLoader()
            ) {{
                defineClass("com.truej.sql.compiler.A", bytes, 0, bytes.length);
            }}.loadClass(className);


            var instance = theClass.newInstance();
            return instance;

        } catch (IOException | URISyntaxException | InstantiationException |
                 IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
