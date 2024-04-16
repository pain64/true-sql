package com.truej.sql.util;

import javax.tools.FileObject;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class StringJsFile implements FileObject {
    private ByteArrayOutputStream out;
    private final String name;

    public StringJsFile(String name) {
        this.name = name;
    }

    @Override
    public URI toUri() {
        return URI.create("string://" + getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return out = new ByteArrayOutputStream();
    }

    @Override
    public CharSequence getCharContent(boolean b) throws IOException {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Override
    public Writer openWriter() throws IOException {
        if (out == null)
            openOutputStream();

        return new PrintWriter(out);
    }


    @Override
    public Reader openReader(boolean b) {
        throw new RuntimeException("not impl");
    }

    @Override
    public InputStream openInputStream() {
        throw new RuntimeException("not impl");
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public boolean delete() {
        return false;
    }
}
