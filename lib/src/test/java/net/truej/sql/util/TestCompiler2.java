package net.truej.sql.util;

import com.sun.tools.javac.api.BasicJavacTask;
import net.truej.sql.compiler.TrueSqlPlugin;

import javax.tools.*;
import java.io.StringWriter;
import java.util.*;

import static java.util.Arrays.asList;

public class TestCompiler2 {
    public record Message(Diagnostic.Kind kind, String text) { }

    public static Map<String, SimpleFileManager.ClassFileData> compile(
        List<? extends SimpleJavaFileObject> compilationUnits,
        List<Message> expectedMessages
    ) {
        var output = new StringWriter();

        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new SimpleFileManager(
            compiler.getStandardFileManager(diagnostic -> {
                var xx = 1;
            }, null, null));

        var arguments = asList(
            "--enable-preview", "--source", "21",
            "-classpath", System.getProperty("java.class.path"),
           // "-Atruesql.printConfig=true",
            "-Xplugin:" + TrueSqlPlugin.NAME
        );

        var hasMessages = new ArrayList<Message>();

        var task = (BasicJavacTask) compiler.getTask(output, fileManager,
            diagnostic -> {
                if (
                    diagnostic.getKind() == Diagnostic.Kind.ERROR ||
                    diagnostic.getKind() == Diagnostic.Kind.WARNING
                )
                    hasMessages.add(new Message(
                        diagnostic.getKind(), diagnostic.getMessage(Locale.ENGLISH)
                    ));

                System.out.println(diagnostic);
            },
            arguments, null, compilationUnits
        );

        if (
            !task.call() && expectedMessages.stream()
                .noneMatch(m -> m.kind == Diagnostic.Kind.ERROR)
        ) {
            System.out.println(output);
            throw new RuntimeException(output.toString());
        }

        var xx = 1;

        for (var expected : expectedMessages)
            if (!hasMessages.contains(expected))
                throw new AssertionError(
                    "Expected compiler message " + expected + " but was not"
                );

        return fileManager.compiled2;
    }
}
