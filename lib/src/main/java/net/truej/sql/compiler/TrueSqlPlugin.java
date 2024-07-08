package net.truej.sql.compiler;

import com.sun.source.util.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.api.BasicJavacTask;
import net.truej.sql.bindings.Standard;
import net.truej.sql.source.NoopInvocation;
import net.truej.sql.source.ParameterExtractor;

import java.util.function.Function;

import static net.truej.sql.compiler.GLangParser.*;
import static net.truej.sql.compiler.StatementGenerator.*;
import static net.truej.sql.compiler.TrueSqlAnnotationProcessor.*;

public class TrueSqlPlugin implements Plugin {
    public static final String NAME = "TrueSql";

    public record Invocation(
        java.util.List<Standard.Binding> bindings,
        String generatedClassName, String fetchMethodName, int lineNumber,
        JCTree.JCIdent sourceExpression, QueryMode queryMode, ParameterMetadata parameterMetadata
    ) { }

    enum ParameterMode {IN, INOUT, OUT, UNKNOWN}

    record ParameterMetadata(
        String javaClassName, String sqlTypeName, int sqlType,
        ParameterMode mode, NullMode nullMode
    ) { }

    // in     - pass as is + rw
    // inout  - pass as is + rw
    // out    - rw
    // unfold - list + rw for each component
    // unfold{2..4}

    @Override public String getName() { return NAME; }

    @Override public void init(JavacTask task, String... args) {
        task.addTaskListener(new TaskListener() {
            @Override public void started(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND) {
                    System.out.println("Annotation processor round");
                }
            }

            @Override public void finished(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANALYZE) {

                    var context = ((BasicJavacTask) task).getContext();
                    var types = Types.instance(context);
                    var symtab = Symtab.instance(context);
                    var resolve = Resolve.instance(context);
                    var names = Names.instance(context);
                    var maker = TreeMaker.instance(context);

                    var clNoop = symtab.getClass(symtab.unnamedModule, names.fromString(
                        NoopInvocation.class.getName()
                    ));

                    var mtNoop = (Symbol.MethodSymbol) clNoop.members()
                        .getSymbolsByName(names.fromString("noop"))
                        .iterator().next();

                    var elements = JavacElements.instance(context);

                    var cuTrees = patchParametersTrees.get(e.getCompilationUnit());

                    if (cuTrees != null) {
                        // 1. getClass - by generated name
                        // 2. get method name + line number
                        // 3. need trees for parameters: batch vs single
                        // 4. check parameter types - parse parameter metadata ???
                        cuTrees.forEach((tree, invocation) -> {

                            var clParameterExtractor = symtab.getClass(
                                symtab.unnamedModule, names.fromString(
                                    ParameterExtractor.class.getName()
                                )
                            );

                            var clGenerated = symtab.getClass(
                                symtab.unnamedModule, names.fromString(invocation.generatedClassName)
                            );

                            var mtGenerated = (Symbol.MethodSymbol) clGenerated.members()
                                .getSymbolsByName(names.fromString(
                                    invocation.fetchMethodName + "__line" + invocation.lineNumber + "__"
                                )).iterator().next();

                            // tree.meth = maker.Select(maker.Ident(clNoop), mtNoop);

                            tree.meth = maker.Select(maker.Ident(clGenerated), mtGenerated);
                            tree.args = List.nil();

                            var createRwFor = (Function<Type, JCTree.JCExpression>) type -> {
                                var forClassName = type.tsym.flatName().toString();

                                var binding = invocation.bindings.stream().filter(b ->
                                    b.className().equals(forClassName)
                                ).findFirst().orElseThrow(() -> new RuntimeException(
                                    "cannot find binding for " + forClassName
                                ));

                                var rwClassSymbol = symtab.getClass(
                                    symtab.unnamedModule, names.fromString(binding.rwClassName())
                                );

                                var rwClassConstructor = (Symbol.MethodSymbol) rwClassSymbol.members().getSymbols(sym ->
                                    sym instanceof Symbol.MethodSymbol m && m.name.equals(names.fromString("<init>")) &&
                                    m.params.isEmpty()
                                ).iterator().next();


                                // return maker.Literal(TypeT);

                                // new rwClassSymbol()
                                var newClass = maker.NewClass(null, List.nil(), maker.Ident(rwClassSymbol), List.nil(), null);
                                newClass.type = new Type.ClassType(Type.noType, List.nil(), rwClassSymbol);
                                newClass.constructor = rwClassConstructor;
                                newClass.constructor.type = rwClassConstructor.type;


                                return newClass;
                            };

                            var boxType = (Function<Type, Type>) t -> {
                                if (t == symtab.charType)
                                    return symtab.getClass(symtab.java_base, names.fromString(Character.class.getName())).type;
                                else if (t == symtab.longType)
                                    return symtab.getClass(symtab.java_base, names.fromString(Long.class.getName())).type;
                                // TODO: all primitive types
                                return t;
                            };


                            var metadataIndex = 0;

                            switch (invocation.queryMode) {
                                case BatchedQuery bq:
                                    tree.args = tree.args.append(bq.listDataExpression());

                                    for (var part : bq.parts())
                                        switch (part) {
                                            case SimpleParameter p:
                                                var extractor = new JCTree.JCLambda(
                                                    List.of(bq.expressionLambda().params.head),
                                                    p.expression()
                                                );
                                                var extractorType = new Type.ClassType(
                                                    Type.noType,
                                                    List.of(
                                                        bq.expressionLambda().params.head.type,
                                                        boxType.apply(p.expression().type)
                                                    ),
                                                    clParameterExtractor
                                                );

                                                extractor.type = extractorType;
                                                extractor.target = extractorType;

                                                tree.args = tree.args.append(extractor);
                                                tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                                                metadataIndex++;
                                                break;
                                            case TextPart _:
                                                break;
                                            case InoutParameter _,
                                                 OutParameter _,
                                                 UnfoldParameter _:
                                                throw new RuntimeException("unreachable");
                                        }
                                    break;
                                case SingleQuery sq:
                                    for (var part : sq.parts())
                                        switch (part) {
                                            case SimpleParameter p:
                                                tree.args = tree.args.append(p.expression());
                                                tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                                                metadataIndex++;
                                                break;
                                            case InoutParameter p:
                                                tree.args = tree.args.append(p.expression());
                                                tree.args = tree.args.append(createRwFor.apply(p.expression().type));
                                                metadataIndex++;
                                                break;
                                            case OutParameter p:
                                                tree.args = tree.args.append(
                                                    createRwFor.apply(
                                                        new Type.ClassType(Type.noType, List.nil(), p.toClass())
                                                    )
                                                );
                                                metadataIndex++;
                                                break;
                                            case UnfoldParameter p:
                                                var unfoldType = p.expression().type.allparams().head;
                                                tree.args = tree.args.append(p.expression());

                                                switch (p.n()) {
                                                    case 1:
                                                        tree.args = tree.args.append(createRwFor.apply(
                                                            unfoldType
                                                        ));
                                                        break;
                                                    case 2:
                                                    case 3:
                                                    case 4:
                                                        for (var attributeType : unfoldType.allparams())
                                                            tree.args = tree.args.append(createRwFor.apply(
                                                                attributeType
                                                            ));
                                                        break;
                                                    default:
                                                        throw new RuntimeException("unreachable");
                                                }

                                                metadataIndex += p.n();
                                                break;
                                            case TextPart _:
                                                break;
                                        }
                                    break;
                            }

                            tree.args = tree.args.append(invocation.sourceExpression);
                            var zzz = 1;
                        });

                        var xxx = 1;
                    }
                }
            }
        });
    }
}
