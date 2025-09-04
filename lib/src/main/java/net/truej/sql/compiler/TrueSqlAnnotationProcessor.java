package net.truej.sql.compiler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;

import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.model.JavacElements;
import net.truej.sql.TrueSql;
import net.truej.sql.compiler.InvocationsHandler.ConstraintCheckTask;
import net.truej.sql.compiler.InvocationsHandler.FetchInvocationTask;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeReadWrite;
import net.truej.sql.fetch.*;
import net.truej.sql.fetch.Parameters;
import net.truej.sql.source.ConnectionW;
import net.truej.sql.source.DataSourceW;

import static net.truej.sql.compiler.TrueSqlPlugin.*;

@SupportedAnnotationTypes({"net.truej.sql.TrueSql", "net.truej.sql.config.Configuration"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class TrueSqlAnnotationProcessor extends AbstractProcessor {

    static final String GENERATED_CLASS_NAME_SUFFIX = "G";

    interface Action<T> {
        T run() throws Exception;
    }
    public static <T> T checkedExceptionAsUnchecked(Action<T> fn) {
        try {
            return fn.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TrueSqlAnnotationProcessor() { }

    private static boolean isDriversLoaded;

    static Connection getConnection(
        String url, String username, String password
    ) throws SQLException {

        if (!isDriversLoaded) {
            isDriversLoaded = true;
            for (var driver : ServiceLoader.load(
                Driver.class, TrueSqlAnnotationProcessor.class.getClassLoader()
            ))
                DriverManager.registerDriver(driver);
        }

        return DriverManager.getConnection(url, username, password);
    }

    @Override public boolean process(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) {

        var env = (JavacProcessingEnvironment) processingEnv;
        var options = env.getOptions();

        var context = env.getContext();
        var symtab = Symtab.instance(context);
        var names = Names.instance(context);
        var elements = JavacElements.instance(context);
        var javacLog = Log.instance(context);
        var messages = new CompilerMessages(context);

        if (annotations.isEmpty()) return false;

        if ("true".equals(ConfigurationParser.findProperty(options, "truesql.printConfig"))) {
            javacLog.printRawLines("TrueSql configuration:");

            for (var element : roundEnv.getElementsAnnotatedWith(Configuration.class)) {
                var found = elements.getTreeAndTopLevel(element, null, null);
                ConfigurationParser.parseConfig(
                    options, symtab, names, found.snd, (Symbol.ClassSymbol) element,
                    (propName, propValue) -> javacLog.printRawLines(
                        "\t" + propName + (propValue == null ? " UNDEFINED" : "=" + propValue)
                    )
                );
            }
        }

        for (var element : roundEnv.getElementsAnnotatedWith(TrueSql.class)) {
            var found = elements.getTreeAndTopLevel(element, null, null);
            var tree = found.fst;
            var cu = found.snd;
            var elementSymbol = (Symbol.ClassSymbol) element;

            @SuppressWarnings("unchecked") var cuInfo = (
                HashMap<
                    JCTree.JCCompilationUnit,
                    LinkedHashMap<JCTree.JCMethodInvocation, Object>
                    >
                ) context.get(HashMap.class);

            if (cuInfo == null) {
                cuInfo = new HashMap<>();
                context.put(HashMap.class, cuInfo);
            }

            var generatedClassFqn = elementSymbol.getQualifiedName() + GENERATED_CLASS_NAME_SUFFIX;
            var dest = new LinkedHashMap<JCTree.JCMethodInvocation, MethodInvocationResult>();
            var tasks = InvocationsFinder.find(
                dest, options, symtab, names, messages, cu, tree, generatedClassFqn
            );

            for (var kv : tasks.stream().collect(
                Collectors.groupingBy(InvocationsHandler.Task::config)).entrySet()
            ) {
                try (
                    var connection = kv.getKey().url() == null ? null : getConnection(
                        kv.getKey().url(), kv.getKey().username(), kv.getKey().password()
                    )
                ) {
                    for (var task : kv.getValue())
                        try {
                            switch (task) {
                                case ConstraintCheckTask t ->
                                    InvocationsHandler.handleConstraint(connection, t);
                                case FetchInvocationTask t ->
                                    dest.put(t.tree(), InvocationsHandler.handleInvocation(
                                        names, connection, t
                                    ));
                            }
                        } catch (
                            GLangParser.ParseException | ExistingDtoParser.ParseException |
                            ValidationException | SQLException e
                        ) {
                            // Эти ошибки нужно отдать в javac именно в этот момент.
                            // Если закончится раунд процессинга аннотаций, а ошибка не записана
                            // то у нас не сгенерируются Dto и будет ошибка компиляции
                            // и до плагина компилятора мы не дойдем!
                            messages.write(cu, task.tree(), JCDiagnostic.DiagnosticType.ERROR, e.getMessage());
                        }
                } catch (SQLException e) {
                    // FIXME: tree -> config.tree
                    messages.write(cu, tree, JCDiagnostic.DiagnosticType.ERROR, e.getMessage());
                }
            }

            cuInfo.put(cu, new LinkedHashMap<>() {{
                for (var kv : dest.entrySet())
                    put(kv.getKey(), doofyEncode(kv.getValue()));
            }});

            checkedExceptionAsUnchecked(() -> {
                try (var out = new PrintWriter(
                    env.getFiler().createSourceFile(generatedClassFqn, element).openWriter()
                )) {

                    out.write("package " +
                              elementSymbol.getQualifiedName().toString()
                                  .replace("." + elementSymbol.getSimpleName().toString(), "") +
                              ";\n"
                    );
                    out.write("import " + TypeReadWrite.class.getName() + ";\n");
                    out.write("import " + ConnectionW.class.getName() + ";\n");
                    out.write("import " + DataSourceW.class.getName() + ";\n");
                    out.write("import " + TooMuchRowsException.class.getName() + ";\n");
                    out.write("import " + TooFewRowsException.class.getName() + ";\n");
                    out.write("import " + UpdateResult.class.getName() + ";\n");
                    out.write("import " + UpdateResultStream.class.getName() + ";\n");
                    out.write("import " + Function.class.getName() + ";\n");
                    out.write("import " + Parameters.class.getName() + ".*;\n");
                    out.write("import " + EvenSoNullPointerException.class.getName() + ";\n");
                    out.write("import " + org.jetbrains.annotations.Nullable.class.getName() + ";\n");
                    out.write("import " + org.jetbrains.annotations.NotNull.class.getName() + ";\n");
                    out.write("import net.truej.sql.bindings.*;\n");
                    out.write("import java.util.List;\n");
                    out.write("import java.util.ArrayList;\n");
                    out.write("import java.util.HashMap;\n");

                    // FIXME: remove this imports
                    out.write("import java.util.stream.Stream;\n");
                    out.write("import java.util.stream.Collectors;\n");
                    out.write("import java.util.Objects;\n");

                    out.write("import java.sql.SQLException;\n");
                    // FIXME: uncomment, set isolating mode for annotation processor
                    // out.write("import jstack.greact.SafeSqlPlugin.Depends;\n\n");
                    // out.write("@Depends(%s.class)\n".formatted(elementSymbol.getQualifiedName()));
                    out.write("%s class %s { \n".formatted(
                        elementSymbol.isPublic() ? "public" : "",
                        elementSymbol.getSimpleName() + GENERATED_CLASS_NAME_SUFFIX
                    ));

                    for (var result : dest.values())
                        if (result instanceof FetchInvocation fi)
                            out.write(fi.generatedCode());

                    out.write("}");
                }

                return null;
            });
        }

        return false;
    }
}
