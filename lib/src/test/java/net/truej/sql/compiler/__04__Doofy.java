package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class __04__Doofy {
    record XXX(int a) {
        public int a() {
            throw new RuntimeException("oops");
        }
    }

    @Test public void test() {
        Assertions.assertThrows(RuntimeException.class, () ->
            TrueSqlPlugin.doofyEncode(new XXX(42))
        );

        Assertions.assertThrows(RuntimeException.class, () ->
            TrueSqlPlugin.doofyDecode(List.of("not.defined.class.A", 42))
        );
    }
}
