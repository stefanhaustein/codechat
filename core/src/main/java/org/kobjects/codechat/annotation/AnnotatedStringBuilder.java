package org.kobjects.codechat.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotatedStringBuilder implements Appendable, AnnotatedCharSequence {
    private final StringBuilder sb;
    private final List<AnnotationSpan> annotations;

    public AnnotatedStringBuilder(StringBuilder sb, List<AnnotationSpan> annotations) {
        this.sb = sb;
        this.annotations = annotations;
    }

    public AnnotatedStringBuilder() {
        this(new StringBuilder(), new ArrayList<AnnotationSpan>());
    }

    @Override
    public AnnotatedStringBuilder append(CharSequence csq) {
        sb.append(csq);
        return this;
    }

    @Override
    public AnnotatedStringBuilder append(CharSequence csq, int start, int end) {
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public AnnotatedStringBuilder append(char c) throws IOException {
        sb.append(c);
        return this;
    }

    public AnnotatedStringBuilder append(CharSequence csq, Link link) {
        int pos = sb.length();
        sb.append(csq);
        if (link != null && annotations != null) {
            annotations.add(new AnnotationSpan(pos, sb.length(), link));
        }
        return this;
    }

    public StringBuilder getStringBuilder() {
        return sb;
    }

    public String toString() {
        return sb.toString();
    }

    public List<AnnotationSpan> getAnnotationList() {
        return annotations;
    }

    public int length() {
        return sb.length();
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    public void addAnnotation(int start, int length, Link link) {
        if (annotations != null) {
            annotations.add(new AnnotationSpan(start, length, link));
        }
    }

    @Override
    public Iterable<AnnotationSpan> getAnnotations() {
        return annotations == null ? Collections.<AnnotationSpan>emptyList() : annotations;
    }

    public AnnotatedCharSequence build() {
        return new AnnotatedString(sb, annotations);
    }
}
