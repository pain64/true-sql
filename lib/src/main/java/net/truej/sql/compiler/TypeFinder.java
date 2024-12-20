package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class TypeFinder {

    static RuntimeException badFormat() {
        return new RuntimeException(
            "expected Name.class or full.qualified.Name.class or array[].class"
        );
    }

    static Type mapArrayOrScalarType(
        Symtab symtab, JCTree.JCCompilationUnit cu, JCTree.JCExpression tree
    ) {
        if (tree instanceof JCTree.JCArrayTypeTree at)
            return new Type.ArrayType(mapArrayOrScalarType(symtab, cu, at.elemtype), symtab.arrayClass);
        else {
            var found = find(symtab, cu, tree);
            if (found == null) throw badFormat();
            return found;
        }
    }

    public static Type resolve(
        Names names, Symtab symtab, JCTree.JCCompilationUnit cu, JCTree.JCExpression tree
    ) {

        if (tree instanceof JCTree.JCFieldAccess fa) {
            if (!fa.name.equals(names.fromString("class")))
                throw badFormat();
            return mapArrayOrScalarType(symtab, cu, fa.selected);
        } else
            throw badFormat();
    }

    public static @Nullable Type find(
        Symtab symtab, JCTree.JCCompilationUnit cu, JCTree.JCExpression tree
    ) {
        Predicate<Symbol> isClass = s -> s instanceof Symbol.ClassSymbol;

        if (tree instanceof JCTree.JCPrimitiveTypeTree primitive) {
            return new Type.JCPrimitiveType(
                primitive.typetag,
                switch (primitive.typetag) {
                    case BYTE -> symtab.byteType.tsym;
                    case CHAR -> symtab.charType.tsym;
                    case SHORT -> symtab.shortType.tsym;
                    case LONG -> symtab.longType.tsym;
                    case FLOAT -> symtab.floatType.tsym;
                    case INT -> symtab.intType.tsym;
                    case DOUBLE -> symtab.doubleType.tsym;
                    case BOOLEAN -> symtab.booleanType.tsym;
                    default -> null;
                }
            );
        } if (tree instanceof JCTree.JCIdent id) {
            var found = new Symbol.ClassSymbol[]{null};
            found[0] = (Symbol.ClassSymbol) cu.toplevelScope.findFirst(id.name, isClass);

            // FIXME: use SimpleTreeVisitor
            if (found[0] == null || found[0].type.isErroneous())
                cu.accept(
                    new TreeScanner() {
                        @Override public void visitClassDef(JCTree.JCClassDecl tree) {
                            if (tree.name.equals(id.name) && tree.sym != null)
                                found[0] = tree.sym;

                            super.visitClassDef(tree);
                        }
                    }
                );

            if (found[0] == null || found[0].type.isErroneous())
                found[0] = (Symbol.ClassSymbol) cu.namedImportScope.findFirst(id.name, isClass);

            if (found[0] == null || found[0].type.isErroneous())
                found[0] = symtab.getClass(cu.modle, cu.packge.fullname.append('.', id.name));

            if (found[0] == null || found[0].type.isErroneous())
                found[0] = (Symbol.ClassSymbol) cu.starImportScope.findFirst(id.name, isClass);

            if (found[0] == null || found[0].type.isErroneous())
                return null;

            return new Type.ClassType(Type.noType, List.nil(), found[0]);

        } else if (tree instanceof JCTree.JCFieldAccess tail) {
            var fqn = tail.name;
            while (true) {
                if (tail.selected instanceof JCTree.JCIdent id) {
                    fqn = id.name.append('.', fqn); break;
                } else if (tail.selected instanceof JCTree.JCFieldAccess tfa) {
                    fqn = tfa.name.append('.', fqn); tail = tfa;
                } else
                    return null;
            }

            var found = symtab.getClass(cu.modle, fqn);

            if (found == null || found.type.isErroneous())
                found = symtab.getClass(symtab.java_base, fqn);

            if (found == null || found.type.isErroneous())
                return null;

            return new Type.ClassType(Type.noType, List.nil(), found);
        } else
            return null;
    }
}
