package net.truej.sql.util;

import com.sun.tools.javac.api.BasicJavacTask;
import net.truej.sql.compiler.TrueSqlPlugin;
import org.jetbrains.annotations.Nullable;

import javax.tools.*;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class TestCompiler {
    
    public record Message(Diagnostic.Kind kind, String text) { }

    public static Map<String, SimpleFileManager.ClassFileData> compile(
        List<? extends SimpleJavaFileObject> compilationUnits,
        List<String> extraArguments, List<Message> expectedMessages,
        @Nullable String containsOutputText
    ) {

        var output = new StringWriter();
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new SimpleFileManager(
            compiler.getStandardFileManager(diagnostic -> { }, null, null)
        );

        var arguments = new ArrayList<String>() {{
            addAll(asList(
                // "--enable-preview",
                "--source", "21",
                "-proc:full",
                "-classpath",
                Arrays.stream(System.getProperty("java.class.path").split(":"))
                    .filter(cp ->
                        cp.contains("true-sql") ||
                        cp.contains("jupiter-api") ||
                        cp.contains("jetbrains") ||
                        cp.contains("org.postgresql") ||
                        cp.contains("apiguardian")
                    ).collect(Collectors.joining(":")),
                "-Xplugin:" + TrueSqlPlugin.NAME
            ));
        }};

        arguments.addAll(extraArguments);

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

        if (containsOutputText != null && !output.toString().contains(containsOutputText))
            throw new RuntimeException("Expected output not exists. Has:\n" + output);

        for (var expected : expectedMessages)
            if (!hasMessages.contains(expected))
                throw new AssertionError(
                    "Expected compiler message " + expected + " but was not"
                );

        return fileManager.compiled2;
    }
}
