package org.kobjects.codechat.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotatedString implements AnnotatedCharSequence {
    private final String s;
    private final List<AnnotationSpan> annotationList;

    public AnnotatedString(CharSequence s, List<AnnotationSpan> annotationList) {
        this.s = String.valueOf(s);
        if (annotationList == null || annotationList.size() == 0) {
            this.annotationList = Collections.emptyList();
        } else {
            this.annotationList = new ArrayList<>(annotationList.size());
            this.annotationList.addAll(annotationList);
        }
    }

    @Override
    public int length() {
        return s.length();
    }

    @Override
    public char charAt(int index) {
        return s.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {

        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append(s.substring(start, end));

        for (AnnotationSpan span : annotationList) {
            int newStart = span.getStart() - start;
            if (newStart < 0) {
                newStart = 0;
            }
            int newEnd = span.getEnd() - start;
            if (newEnd > asb.length()) {
                newEnd = asb.length();
            }

            if (newStart < asb.length() && newEnd > 0 && newEnd > newStart) {
                asb.addAnnotation(newStart, newEnd, span.getAnnotation());
            }
        }

        return asb.build();
    }

    @Override
    public String toString() {
        return s;
    }

    @Override
    public Iterable<AnnotationSpan> getAnnotations() {
        return annotationList;
    }
}
