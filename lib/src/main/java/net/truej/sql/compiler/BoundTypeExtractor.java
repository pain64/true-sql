package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.HashMap;
import java.util.function.Consumer;

public class BoundTypeExtractor {

    private static Type.ClassType up(
        Symbol.ClassSymbol typeReadWriteSymbol, HashMap<Symbol, Type> map, Symbol.ClassSymbol from) {

        var saveTypeArguments = (Consumer<Type>) t -> {
            for (var i = 0; i < t.tsym.getTypeParameters().size(); i++) {
                var typeArg = t.getTypeArguments().get(i);
                var prev = map.get(typeArg.tsym);

                map.put(
                    t.tsym.getTypeParameters().get(i),
                    prev != null ? prev : typeArg
                );
            }
        };

        if (from == typeReadWriteSymbol)
            return (Type.ClassType) map.get(from.getTypeParameters().getFirst());
        else {
            for (var iface : from.getInterfaces()) {
                if (iface instanceof Type.ClassType cl) {
                    saveTypeArguments.accept(iface);
                    var r = up(typeReadWriteSymbol, map, (Symbol.ClassSymbol) cl.tsym);
                    if (r != null) return r;
                }
            }

            if (from.getSuperclass() instanceof Type.ClassType parent) {
                saveTypeArguments.accept(parent);
                return up(typeReadWriteSymbol, map, (Symbol.ClassSymbol) parent.tsym);
            }

            return null;
        }
    }

    public static Type.ClassType extract(
        Symbol.ClassSymbol typeReadWriteSymbol, Symbol.ClassSymbol from
    ) {
        return up(typeReadWriteSymbol, new HashMap<>(), from);
    }
}
