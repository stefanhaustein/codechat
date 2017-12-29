package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.AnnotationSpan;

public interface Documented {
    void printDocumentation(AnnotatedStringBuilder asb);
}
