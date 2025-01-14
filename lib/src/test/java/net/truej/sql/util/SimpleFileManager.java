package net.truej.sql.util;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SimpleFileManager
    extends ForwardingJavaFileManager<StandardJavaFileManager> {

    public static class ClassFileData extends SimpleJavaFileObject {
        public final ByteArrayOutputStream data =
            new ByteArrayOutputStream();

        public ClassFileData(URI uri, Kind kind) {
            super(uri, kind);
        }

        @Override public OutputStream openOutputStream() {
            return data;
        }

        @Override public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(data.toByteArray());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return data.toString();
        }
    }

    public final Map<String, ClassFileData> compiled2 = new HashMap<>();

    public SimpleFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
    }

    @Override public JavaFileObject getJavaFileForOutput(
        Location location, String className, JavaFileObject.Kind kind, FileObject sibling
    ) {
        var d = new ClassFileData(URI.create("file://" + className + (kind == JavaFileObject.Kind.SOURCE ? ".java" : ".class")), kind);
        compiled2.put(className, d);
        return d;
    }
}
