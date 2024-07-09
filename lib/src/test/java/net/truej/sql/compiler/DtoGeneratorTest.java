package net.truej.sql.compiler;

import net.truej.sql.compiler.GLangParser.*;
import net.truej.sql.compiler.DtoGenerator.*;
import net.truej.sql.compiler.StatementGenerator.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DtoGeneratorTest {

    @Test void oneField() {
        var out = new StatementGenerator.Out(new StringBuilder());

        DtoGenerator.generate(out, new GLangParser.AggregatedType("A", List.of(
            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.String"), "f1")
        )));

        Assertions.assertEquals(
            """
                
                public static class A {
                    public final java.lang.String f1;
                            
                    public A(
                        java.lang.String f1
                    ) {
                        this.f1 = f1;
                    }
                            
                    @Override public boolean equals(Object other) {
                        return this == other || (
                            other instanceof A o &&
                            java.util.Objects.equals(this.f1, o.f1)
                        );
                    }
                            
                    @Override public int hashCode() {
                        int h = 1;
                        h = h * 59 + java.util.Objects.hashCode(this.f1);
                        return h;
                    }
                }""", out.buffer.toString()
        );
    }

    @Test void manyFields() {

        var out = new StatementGenerator.Out(new StringBuilder());

        DtoGenerator.generate(out, new AggregatedType("A", List.of(
            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "java.lang.String"), "f1"),
            new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "float"), "f2"),
            new Field(
                new AggregatedType("B", List.of(
                    new Field(new ScalarType(NullMode.EXACTLY_NOT_NULL, "float"), "f4")
                )),
                "f3"
            )
        )));

        Assertions.assertEquals(
            """
                
                public static class B {
                    public final float f4;
                                
                    public B(
                        float f4
                    ) {
                        this.f4 = f4;
                    }
                                
                    @Override public boolean equals(Object other) {
                        return this == other || (
                            other instanceof B o &&
                            java.util.Objects.equals(this.f4, o.f4)
                        );
                    }
                                
                    @Override public int hashCode() {
                        int h = 1;
                        h = h * 59 + java.util.Objects.hashCode(this.f4);
                        return h;
                    }
                }
                public static class A {
                    public final java.lang.String f1;
                    public final float f2;
                    public final List<B> f3;
                                
                    public A(
                        java.lang.String f1,
                        float f2,
                        List<B> f3
                    ) {
                        this.f1 = f1;
                        this.f2 = f2;
                        this.f3 = f3;
                    }
                                
                    @Override public boolean equals(Object other) {
                        return this == other || (
                            other instanceof A o &&
                            java.util.Objects.equals(this.f1, o.f1) &&
                            java.util.Objects.equals(this.f2, o.f2) &&
                            java.util.Objects.equals(this.f3, o.f3)
                        );
                    }
                                
                    @Override public int hashCode() {
                        int h = 1;
                        h = h * 59 + java.util.Objects.hashCode(this.f1);
                        h = h * 59 + java.util.Objects.hashCode(this.f2);
                        h = h * 59 + java.util.Objects.hashCode(this.f3);
                        return h;
                    }
                }""", out.buffer.toString()
        );
    }
}
