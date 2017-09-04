package org.kobjects.codechat.annotation;

public interface AnnotatedCharSequence extends CharSequence {

    Iterable<AnnotationSpan> getAnnotations();

}
