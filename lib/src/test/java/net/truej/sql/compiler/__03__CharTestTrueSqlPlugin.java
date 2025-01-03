package net.truej.sql.compiler;

import com.sun.tools.javac.code.TypeTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.sun.tools.javac.code.Type;

public class __03__CharTestTrueSqlPlugin {
    @Test public void test() {
        Assertions.assertEquals(
            "char",
            TrueSqlPlugin.arrayClassNameToSourceCodeType("Char")
        );

        Assertions.assertEquals(
            "C",
            TrueSqlPlugin.arrayTypeToClassName(new Type.JCPrimitiveType(TypeTag.CHAR, null))
        );
    }
}
