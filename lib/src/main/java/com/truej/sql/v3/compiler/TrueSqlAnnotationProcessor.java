package com.truej.sql.v3.compiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import com.sun.tools.javac.tree.JCTree;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;

@SupportedAnnotationTypes("com.truej.sql.v3.TrueSql.Process")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class TrueSqlAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var env = (JavacProcessingEnvironment) processingEnv;
        var context = env.getContext();
        var trees = Trees.instance(env);

        if (annotations.isEmpty()) return false;

        var annotatedElements
            = roundEnv.getElementsAnnotatedWith(annotations.iterator().next());

        for (var element : annotatedElements) {
            var tree = (JCTree) trees.getTree(element);
            System.out.println(tree);
        }

        return false;
    }
}
