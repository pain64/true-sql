package net.truej.sql.compiler;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.*;

import java.util.Locale;
import java.util.ResourceBundle;

public class CompilerMessages {
    private final Log javacLog;
    private final JCDiagnostic.Factory diagnosticsFactory;

    public CompilerMessages(Context context) {
        this.javacLog = Log.instance(context);
        this.diagnosticsFactory = JCDiagnostic.Factory.instance(context);

        var javacMessages = JavacMessages.instance(context);

        var bundle = ResourceBundle.getBundle(
            "net.truej.sql.compiler.MessagesBundle_en", new Locale("en", "US")
        );
        javacMessages.add(locale -> bundle);
    }

    public void write(
        JCTree.JCCompilationUnit cu, JCTree forTree,
        JCDiagnostic.DiagnosticType type, String message
    ) {

        javacLog.report(
            diagnosticsFactory.create(
                new DiagnosticSource(cu.getSourceFile(), javacLog),
                new JCDiagnostic.SimpleDiagnosticPosition(forTree.pos),
                JCDiagnostic.DiagnosticInfo.of(type, "compiler", "tsql", message)
            )
        );
    }
}
