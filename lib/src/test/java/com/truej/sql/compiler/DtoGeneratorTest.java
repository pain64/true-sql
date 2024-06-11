package com.truej.sql.compiler;

import com.truej.sql.v3.compiler.DtoGenerator;
import com.truej.sql.v3.compiler.GLangParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.truej.sql.v3.compiler.GLangParser.*;

public class DtoGeneratorTest {

    @Test void oneField() {
        Assertions.assertEquals(
            """
                class A {
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
                }
                """,
            DtoGenerator.generateDto("A", List.of(
                new Field(new ScalarType("java.lang.String"), "f1")
            ))
        );
    }

    @Test void manyFields() {
        Assertions.assertEquals(
            """
                class A {
                    public final java.lang.String f1;
                    public final float f2;
                                
                    public A(
                        java.lang.String f1,
                        float f2
                    ) {
                        this.f1 = f1;
                        this.f2 = f2;
                    }
                                
                    @Override public boolean equals(Object other) {
                        return this == other || (
                            other instanceof A o &&
                            java.util.Objects.equals(this.f1, o.f1) &&
                            java.util.Objects.equals(this.f2, o.f2)
                        );
                    }
                                
                    @Override public int hashCode() {
                        int h = 1;
                        h = h * 59 + java.util.Objects.hashCode(this.f1);
                        h = h * 59 + java.util.Objects.hashCode(this.f2);
                        return h;
                    }
                }
                """,
            DtoGenerator.generateDto("A", List.of(
                new Field(new ScalarType("java.lang.String"), "f1"),
                new Field(new ScalarType("float"), "f2")
            ))
        );
    }
}
