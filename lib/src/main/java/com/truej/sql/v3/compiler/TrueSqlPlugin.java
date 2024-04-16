package com.truej.sql.v3.compiler;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

public class  TrueSqlPlugin implements Plugin {
    public static final String NAME = "TrueSql";

    @Override public String getName() { return NAME; }

    @Override public void init(JavacTask task, String... args) {
        task.addTaskListener(new TaskListener() {
            @Override public void started(TaskEvent e) {
                if (e.getKind() == TaskEvent.Kind.ANNOTATION_PROCESSING_ROUND) {
                    System.out.println("Annotation processor round");
                }
            }
        });
    }
}
