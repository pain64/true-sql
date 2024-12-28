package net.truej.sql.compiler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.model.JavacElements;
import net.truej.sql.TrueSql;
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

    @Override public boolean process(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) {

        var env = (JavacProcessingEnvironment) processingEnv;
        var context = env.getContext();
        var symtab = Symtab.instance(context);
        var names = Names.instance(context);
        var elements = JavacElements.instance(context);
        var javacLog = Log.instance(context);
        var messages = new CompilerMessages(context);

        if (annotations.isEmpty()) return false;

        if ("true".equals(ConfigurationParser.findProperty("truesql.printConfig"))) {
            javacLog.printRawLines("TrueSql configuration:");

            for (var element : roundEnv.getElementsAnnotatedWith(Configuration.class)) {
                var found = elements.getTreeAndTopLevel(element, null, null);
                try {
                    ConfigurationParser.parseConfig(
                        symtab, names, found.snd, (Symbol.ClassSymbol) element,
                        (propName, propValue) -> javacLog.printRawLines("\t" + propName + "=" + propValue)
                    );
                } catch (ConfigurationParser.ParseException ignored) { }
            }
        }

        var clProcessed = symtab.enterClass(
            symtab.unnamedModule, names.fromString(Processed.class.getName())
        );

        for (var element : roundEnv.getElementsAnnotatedWith(TrueSql.class)) {
            var found = elements.getTreeAndTopLevel(element, null, null);
            var tree = found.fst;
            var cu = found.snd;
            var elementSymbol = (Symbol.ClassSymbol) element;
            var maker = TreeMaker.instance(context);

            var pkgCompiler = (Symbol.PackageSymbol) clProcessed.owner;
            var pkgSql = (Symbol.PackageSymbol) pkgCompiler.owner;
            var pkgTruej = (Symbol.PackageSymbol) pkgSql.owner;
            var pkgNet = (Symbol.PackageSymbol) pkgTruej.owner;

            ((JCTree.JCClassDecl) tree).mods.annotations = ((JCTree.JCClassDecl) tree).mods.annotations.prepend(
                maker.Annotation(
                    maker.Select(
                        maker.Select(
                            maker.Select(maker.Select(maker.Ident(pkgNet), pkgTruej), pkgSql),
                            pkgCompiler
                        ),
                        clProcessed
                    ), List.nil()
                )
            );

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
            var invocations = InvocationsFinder.find(
                symtab, names, messages, cu, tree, generatedClassFqn
            );

            cuInfo.put(cu, new LinkedHashMap<>() {{
                for (var kv : invocations.entrySet())
                    put(kv.getKey(), doofyEncode(kv.getValue()));
            }});

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
                out.write("import java.util.stream.Stream;\n");
                out.write("import java.util.stream.Collectors;\n");
                out.write("import java.util.Objects;\n");
                out.write("import java.sql.SQLException;\n");
                // FIXME: uncomment, set isolating mode for annotation processor
                // out.write("import jstack.greact.SafeSqlPlugin.Depends;\n\n");
                // out.write("@Depends(%s.class)\n".formatted(elementSymbol.getQualifiedName()));
                out.write("class %s { \n".formatted(
                    elementSymbol.getSimpleName() + GENERATED_CLASS_NAME_SUFFIX
                ));

                for (var result : invocations.values())
                    if (result instanceof FetchInvocation fi)
                        out.write(fi.generatedCode());

                out.write("}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }
}
