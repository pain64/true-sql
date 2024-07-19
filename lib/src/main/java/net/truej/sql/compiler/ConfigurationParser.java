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
    public record ParsedConfiguration(
        String url, String username, String password, List<Standard.Binding> typeBindings
    ) { }

    public static @Nullable String findProperty(ProcessingEnvironment env, String propertyName) {
        var prop = env.getOptions().get(propertyName);
        if (prop != null) return prop;

        prop = System.getProperty(propertyName);
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
                var propertyName = STR."truesql.\{sourceClassName}.\{parameterName}";
                var prop = findProperty(env, propertyName);

                if (prop == null)
                    if (!defaultValue.equals(Configuration.STRING_NOT_DEFINED))
                        prop = defaultValue;

                if (doPrint) System.out.println(STR."\{propertyName}=\{prop}");

                return prop;
            };

        if (sourceConfig != null) {
            var bindings = new ArrayList<>(Standard.bindings);

            for (var tb : sourceConfig.typeBindings()) {
                Type.ClassType rwClassType;

                try {
                    rwClassType =
                        new Type.ClassType(
                            null, com.sun.tools.javac.util.List.nil(),
                            symtab.getClass(cu.modle, names.fromString(tb.rw().getName()))
                        );
                } catch (MirroredTypeException mte) {
                    rwClassType = ((Type.ClassType) mte.getTypeMirror());
                }

                var bound = BoundTypeExtractor.extract(
                    symtab.getClass(cu.modle, names.fromString(TypeReadWrite.class.getName())),
                    rwClassType
                );

                var emptyArgsConstructor = ((Symbol.ClassSymbol) rwClassType.tsym).members_field.getSymbols(sym ->
                    sym instanceof Symbol.MethodSymbol m && m.name.equals(names.fromString("<init>")) && m.params.isEmpty()
                ).iterator();

                if (!emptyArgsConstructor.hasNext())
                    throw new RuntimeException(
                        "rw class " + rwClassType.tsym.flatName().toString() + " has no no-arg constructor"
                    );


                bindings.add(new Standard.Binding(
                    bound.tsym.flatName().toString(),
                    rwClassType.tsym.flatName().toString(),
                    tb.mayBeNullable(),
                    tb.compatibleSqlType() != INT_NOT_DEFINED ? tb.compatibleSqlType() : null,
                    !tb.compatibleSqlTypeName().equals(STRING_NOT_DEFINED) ? tb.compatibleSqlTypeName() : null
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
