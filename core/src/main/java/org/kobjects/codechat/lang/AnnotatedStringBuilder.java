package org.kobjects.codechat.lang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnnotatedStringBuilder implements Appendable, CharSequence {
    private final StringBuilder sb;
    private final List<Annotation> annotations;

    public AnnotatedStringBuilder(StringBuilder sb, List<Annotation> annotations) {
        this.sb = sb;
        this.annotations = annotations;
    }

    public AnnotatedStringBuilder() {
        this(new StringBuilder(), new ArrayList<Annotation>());
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

    public AnnotatedStringBuilder append(CharSequence csq, Object o) {
        int pos = sb.length();
        sb.append(csq);
        if (o != null && annotations != null) {
            annotations.add(new Annotation(pos, sb.length(), o));
        }
        return this;
    }

    public StringBuilder getStringBuilder() {
        return sb;
    }

    public String toString() {
        return sb.toString();
    }

    public List<Annotation> getAnnotationList() {
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
}
