package net.truej.sql.compiler;

import java.util.ListResourceBundle;

public class MessagesBundle_en extends ListResourceBundle {

    @Override protected Object[][] getContents() {
        return contents;
    }

    private final Object[][] contents = {
        {"compiler.err.tsql", "{0}"},
        {"compiler.warn.tsql", "{0}"},
    };
}
