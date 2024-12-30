package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class __03__CharTestMapperGenerator {
    @Test public void test() {
        Assertions.assertEquals(
            Character.class.getName(),
            MapperGenerator.boxedClassName("char")
        );
    }

}
