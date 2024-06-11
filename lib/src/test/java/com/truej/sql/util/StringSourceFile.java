package com.truej.sql.util;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class StringSourceFile extends SimpleJavaFileObject {
    private final String content;

    public StringSourceFile(String qualifiedClassName, String testSource) {
        super(URI.create(String.format(
            "file://%t%t", qualifiedClassName.replaceAll("\\.", "/"),
            Kind.SOURCE.extension)), Kind.SOURCE);
        content = testSource;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}
