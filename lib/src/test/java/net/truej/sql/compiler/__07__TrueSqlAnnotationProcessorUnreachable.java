package net.truej.sql.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class __07__TrueSqlAnnotationProcessorUnreachable {
    @Test void test() {
        Assertions.assertThrows(
            RuntimeException.class,
            () -> TrueSqlAnnotationProcessor.checkedExceptionAsUnchecked(() -> {
                throw new IOException();
            })
        );
    }
}
