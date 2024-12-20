package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Names;
import net.truej.sql.bindings.Standard;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeReadWrite;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.MirroredTypeException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static net.truej.sql.config.Configuration.INT_NOT_DEFINED;
import static net.truej.sql.config.Configuration.STRING_NOT_DEFINED;

public class ConfigurationParser {
    public static class ParseException extends RuntimeException {
        public ParseException(String message) { super(message); }
    }
    public record ParsedConfiguration(
        String url, String username, String password, List<Standard.Binding> typeBindings
    ) { }

    public static @Nullable String findProperty(ProcessingEnvironment env, String propertyName) {
//        var prop = env.getOptions().get(propertyName);
//        if (prop != null) return prop;

        var prop = System.getProperty(propertyName);
        if (prop != null) return prop;

        prop = System.getenv(propertyName);
        return prop;
    }

    public static ParsedConfiguration parseConfig(
        ProcessingEnvironment env, Symtab symtab, Names names,
        JCTree.JCCompilationUnit cu, String sourceClassName,
        Configuration sourceConfig, boolean doPrint
    ) {
        var parseProperty = (BiFunction<String, String, String>)
            (parameterName, defaultValue) -> {
                var propertyName = "truesql." + sourceClassName + "." + parameterName;
                var prop = findProperty(env, propertyName);

                if (prop == null)
                    if (!defaultValue.equals(Configuration.STRING_NOT_DEFINED))
                        prop = defaultValue;

                if (doPrint) System.out.println(propertyName + "=" + prop);

                return prop;
            };

        if (sourceConfig != null) {
            var bindings = new ArrayList<>(Standard.bindings);

            for (var tb : sourceConfig.typeBindings()) {

                Symbol.ClassSymbol rwClassSym;

                try {
                    rwClassSym = symtab.enterClass(cu.modle, names.fromString(tb.rw().getName()));
                } catch (MirroredTypeException mte) {
                    rwClassSym = symtab.enterClass(
                        cu.modle, ((Type.ClassType) mte.getTypeMirror()).tsym.flatName()
                    );
                }

                rwClassSym.complete();

                var bound = BoundTypeExtractor.extract(
                    symtab.getClass(cu.modle, names.fromString(TypeReadWrite.class.getName())),
                    rwClassSym
                );

                var errors = new ArrayList<String>();
                if (rwClassSym.isAbstract())
                    errors.add("cannot be abstract");

                if (rwClassSym.isInner() && !rwClassSym.isStatic())
                    errors.add("if inner then required to be static also");

                if (!rwClassSym.members_field.anyMatch(sym ->
                    sym instanceof Symbol.MethodSymbol m &&
                    m.name.equals(names.fromString("<init>")) &&
                    m.params.isEmpty() &&
                    m.isPublic()
                ))
                    errors.add("must have public no-arg constructor");

                if (!errors.isEmpty())
                    throw new ParseException(
                        "For source " + sourceClassName + ": " +
                        "RW class " + rwClassSym.flatName() + ": " +
                        String.join(", ", errors)
                    );

                var compatibleSqlType = tb.compatibleSqlType() == INT_NOT_DEFINED
                    ? null : tb.compatibleSqlType();
                var compatibleSqlTypeName = tb.compatibleSqlTypeName().equals(STRING_NOT_DEFINED)
                    ? null : tb.compatibleSqlTypeName();

                if (compatibleSqlType != null && compatibleSqlTypeName != null)
                    throw new ParseException(
                        "For source " + sourceClassName + ": " +
                        "compatibleSqlType and compatibleSqlTypeName " +
                        "cannot be specified at the same time"
                    );

                bindings.add(new Standard.Binding(
                    bound.tsym.flatName().toString(),
                    rwClassSym.flatName().toString(),
                    compatibleSqlType,
                    compatibleSqlTypeName
                ));
            }

            return new ParsedConfiguration(
                parseProperty.apply("url", sourceConfig.checks().url()),
                parseProperty.apply("username", sourceConfig.checks().username()),
                parseProperty.apply("password", sourceConfig.checks().password()),
                bindings
            );
        } else
            return new ParsedConfiguration(null, null, null, Standard.bindings);
    }
}
