package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.AnnotationSpan;

public class SimpleDocumented implements Documented {
    private final AnnotatedString s;

    public SimpleDocumented(String s) {
        this.s = new AnnotatedString(s, null);
    }

    @Override
    public AnnotatedCharSequence getDocumentation() {
        return s;
    }
}
