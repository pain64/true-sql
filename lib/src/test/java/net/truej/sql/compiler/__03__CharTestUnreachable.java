package net.truej.sql.compiler;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class __03__CharTestUnreachable {
    @Test public void test() {
        Assertions.assertEquals(
            Character.class.getName(),
            MapperGenerator.boxedClassName("char")
        );
        Assertions.assertEquals(
            "char",
            TrueSqlPlugin.arrayClassNameToSourceCodeType("Char")
        );

        Assertions.assertEquals(
            "C",
            TrueSqlPlugin.arrayTypeToClassName(new Type.JCPrimitiveType(TypeTag.CHAR, null))
        );

        Assertions.assertSame(
            Character.class,
            TrueSqlPlugin.primitiveTypeToBoxedClass(new Type.JCPrimitiveType(TypeTag.CHAR, null))
        );
    }

}
