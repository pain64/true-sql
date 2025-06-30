package net.truej.sql.compiler;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Names;
import net.truej.sql.bindings.Standard;
import net.truej.sql.config.Configuration;
import net.truej.sql.config.TypeReadWrite;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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

    public static @Nullable String findProperty(
        Map<String, String> annotationProcessorOptions, String propertyName
    ) {
        var prop = annotationProcessorOptions.get(propertyName);
        if (prop != null) return prop;

        prop = System.getProperty(propertyName);
        if (prop != null) return prop;

        prop = System.getenv(propertyName);
        return prop;
    }

    public static ParsedConfiguration parseConfig(
        Map<String, String> annotationProcessorOptions,
        Symtab symtab, Names names, JCTree.JCCompilationUnit cu,
        Symbol.ClassSymbol annotated, BiConsumer<String, String> onPropertyParsed
    ) {
        var parseProperty = (BiFunction<String, String, String>)
            (parameterName, valueInAnnotation) -> {
                var propertyName = "truesql." + annotated.className() + "." + parameterName;
                var prop = findProperty(annotationProcessorOptions, propertyName);

                if (prop == null)
                    if (!valueInAnnotation.equals(Configuration.STRING_NOT_DEFINED))
                        prop = valueInAnnotation;

                onPropertyParsed.accept(propertyName, prop);
                return prop;
            };

        var config = (Configuration) null;
        var typeBindingAttrib = (Attribute.Array) null;

        for (var am : annotated.getAnnotationMirrors())
            if (am.getAnnotationType().toString().equals(Configuration.class.getName())) {
                config = annotated.getAnnotation(Configuration.class);
                typeBindingAttrib = (Attribute.Array) am.member(names.fromString("typeBindings"));
            }

        if (config != null) {
            var bindings = new ArrayList<>(Standard.bindings);

            for (var i = 0; i < config.typeBindings().length; i++) {
                var tb = config.typeBindings()[i];

                var rwClassSym = symtab.enterClass(
                    cu.modle, ((Attribute.Class)
                        ((Attribute.Compound) typeBindingAttrib.values[i])
                            .member(names.fromString("rw"))
                    ).classType.tsym.flatName()
                );

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
                        "For source " + annotated.className() + ": " +
                        "RW class " + rwClassSym.flatName() + ": " +
                        String.join(", ", errors)
                    );

                var compatibleSqlType = tb.compatibleSqlType() == INT_NOT_DEFINED
                    ? null : tb.compatibleSqlType();
                var compatibleSqlTypeName = tb.compatibleSqlTypeName().equals(STRING_NOT_DEFINED)
                    ? null : tb.compatibleSqlTypeName();

                if (compatibleSqlType != null && compatibleSqlTypeName != null)
                    throw new ParseException(
                        "For source " + annotated.className() + ": " +
                        "compatibleSqlType and compatibleSqlTypeName " +
                        "cannot be specified at the same time"
                    );

                bindings.add(new Standard.Binding(
                    TrueSqlPlugin.typeToClassName(bound),
                    rwClassSym.flatName().toString(),
                    compatibleSqlType,
                    compatibleSqlTypeName
                ));
            }

            return new ParsedConfiguration(
                parseProperty.apply("url", config.checks().url()),
                parseProperty.apply("username", config.checks().username()),
                parseProperty.apply("password", config.checks().password()),
                bindings
            );
        } else
            return new ParsedConfiguration(null, null, null, Standard.bindings);
    }
}
