package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class __03__CharTestTrueSqlPlugin {
    @Test public void test() {
        Assertions.assertEquals(
            "char",
            TrueSqlPlugin.arrayClassNameToSourceCodeType("Char")
        );
    }
}
