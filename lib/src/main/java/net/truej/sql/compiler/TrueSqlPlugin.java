package net.truej.sql.compiler;

import com.sun.source.util.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.api.BasicJavacTask;
import net.truej.sql.source.NoopInvocation;

public class TrueSqlPlugin implements Plugin {
    public static final String NAME = "TrueSql";

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

                    var cuTrees = TrueSqlAnnotationProcessor.pathParametersTrees.get(e.getCompilationUnit());

                    if (cuTrees != null) {
                        cuTrees.forEach((invocation, queryMode) -> {
                             invocation.args = List.nil();
                            invocation.meth = maker.Select(maker.Ident(clNoop), mtNoop);
                            //invocation.meth = mtNoop;
                        });
                        var xxx = 1;
                    }

//                    System.out.println("Trees for patch");
//                    System.out.println(TrueSqlAnnotationProcessor.pathParametersTrees);
                }
            }
        });
    }
}
