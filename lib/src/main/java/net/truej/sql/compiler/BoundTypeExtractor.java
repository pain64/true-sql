package net.truej.sql.compiler;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

public class BoundTypeExtractor {

    private static Type.ClassType up(
        Symbol.ClassSymbol typeReadWriteSymbol, Type.ClassType from) {

        if (from.tsym == typeReadWriteSymbol)
            return (Type.ClassType) from.getTypeArguments().head;
        else {
            if (from.interfaces_field != null)
                for (var iface : from.interfaces_field) {
                    if (iface instanceof Type.ClassType cl) {
                        var r = up(typeReadWriteSymbol, cl);
                        if (r != null) return r;
                    }
                }

            if (from.supertype_field instanceof Type.ClassType parent)
                return up(typeReadWriteSymbol, parent);

            return null;
        }
    }

    public static Type.ClassType extract(
        Symbol.ClassSymbol typeReadWriteSymbol, Type.ClassType from
    ) {
        return up(typeReadWriteSymbol, from);
    }
}
