package com.truej.sql.v3.compiler;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.truej.sql.v3.config.Configuration;

public class SourceConfigurationFinder {
    // type of DataSourceW or m(T.class) resolution scheme:
    // def resolveType(name | full qualified) -> TypeSymbol
    //  0. local
    //  1. package-local
    //  2. imports
    //  3. wildcard imports ???
    //  4. java.lang.* ???
    // Test infra needed ???
    Configuration find(JCTree.JCCompilationUnit cu, JCTree.JCIdent sourceVar) {
        new TreeScanner() {
            // ((JCIdent)tree.params.get(0).vartype).type.tsym.getAnnotation(Configuration.class).checks().password()
            // var xxx = 1;
            // local var or class field
            @Override public void visitVarDef(JCTree.JCVariableDecl tree) {
                super.visitVarDef(tree);
            }
            @Override public void visitMethodDef(JCTree.JCMethodDecl tree) {
                super.visitMethodDef(tree);
            }
            @Override public void visitApply(JCTree.JCMethodInvocation tree) {
                // withConnection(ds, cn -> {})
                // find type of ds -> задача изначальная
                super.visitApply(tree);
            }
        }.scan(cu);

        return null; // FIXME
    }
}
