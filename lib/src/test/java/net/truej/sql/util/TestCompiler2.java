package net.truej.sql.util;

import com.sun.tools.javac.api.BasicJavacTask;
import net.truej.sql.compiler.TrueSqlPlugin;

import javax.tools.*;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class TestCompiler2 {
    public static Map<String, SimpleFileManager.ClassFileData> compile(
        List<? extends SimpleJavaFileObject> compilationUnits
    ) {
        var output = new StringWriter();

        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new SimpleFileManager(
            compiler.getStandardFileManager(new DiagnosticListener<JavaFileObject>() {
                @Override public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                    var xx = 1;
                }
            }, null, null));

        var arguments = asList(
            "--enable-preview", "--source", "21",
            "-classpath", System.getProperty("java.class.path"),
            "-Atruesql.printConfig=true",
            "-Xplugin:" + TrueSqlPlugin.NAME
        );

        var task = (BasicJavacTask) compiler.getTask(output, fileManager,
            diagnostic ->
                System.out.println(diagnostic.toString()),
            arguments, null, compilationUnits);

        if (!task.call()) {
            System.out.println(output);
            throw new RuntimeException(output.toString());
        }

        return fileManager.compiled2;
    }
}
