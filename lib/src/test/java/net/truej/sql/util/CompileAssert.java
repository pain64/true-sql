package net.truej.sql.util;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompileAssert {
//    public static void assertCompiled(String javaSrc, String jsDest) throws IOException {
//        assertGenerated(new CompileCase("js.Test", javaSrc, jsDest));
//    }
//
//    public static class CompileCase {
//        final String fullQualified;
//        final String javaSrc;
//        final String jsDest;
//
//        public CompileCase(String fullQualified, String javaSrc, String jsDest) {
//            this.fullQualified = fullQualified;
//            this.javaSrc = javaSrc;
//            this.jsDest = jsDest;
//        }
//    }
    public record Generated(String fullQualified, String code) {}

    public static void assertGenerated(
        String source, Generated... generated
    ) throws IOException {
//        var compilationUnits = Arrays.stream(tests)
//            .map(t -> new StringSourceFile(t.fullQualified, t.javaSrc))
//            .collect(Collectors.toList());
        var compilationUnits = List.of(
            new StringSourceFile("A", """
                package xxx;
                class A {}
                """
            ),
            new StringSourceFile("Test.java", source)
        );

        var res = TestCompiler.compile(compilationUnits);

        for (var g : generated) {
            var javaOut = res.get(g.fullQualified + ".js");
            Assertions.assertEquals(g.code, javaOut.getCharContent(true));
        }
    }
}
