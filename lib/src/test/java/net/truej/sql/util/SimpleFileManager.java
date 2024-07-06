package net.truej.sql.util;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SimpleFileManager
    extends ForwardingJavaFileManager<StandardJavaFileManager> {

    // TODO: unused ???
    public static class NopClassFile extends SimpleJavaFileObject {
        public NopClassFile(URI uri) {
            super(uri, Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return OutputStream.nullOutputStream();
        }
    }

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

    private Map<String, StringJsFile> compiled = new HashMap<>();
    public final Map<String, ClassFileData> compiled2 = new HashMap<>();

    public SimpleFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
    }


    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className, JavaFileObject.Kind kind, FileObject sibling) {
        var d = new ClassFileData(URI.create("string://" + className), kind);
        compiled2.put(className, d);
        return d;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        var name = packageName + "." + relativeName;
        var file = new StringJsFile(name);
        compiled.put(name, file);
        return file;
    }

    public Map<String, StringJsFile> getCompiled() {
        return compiled;
    }
}
