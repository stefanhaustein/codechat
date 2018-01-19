package org.kobjects.codechat.annotation;

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
        this.append(csq, 0, csq.length());
        return this;
    }

    /**
     * If the appended character sequence is an instance of AnnotatedCharSequence,
     * annotations that are fully contained between start and end are carried over.
     */
    @Override
    public AnnotatedStringBuilder append(CharSequence csq, int start, int end) {
        int offset = length();
        sb.append(csq, start, end);
        if (annotations != null && csq instanceof AnnotatedCharSequence) {
            for (AnnotationSpan span : ((AnnotatedCharSequence) csq).getAnnotations()) {
                if (span.getStart() >= start && span.getEnd() <= end) {
                    annotations.add(new AnnotationSpan(
                            span.getStart() - start + offset,
                            span.getEnd() - start + offset,
                            span.getAnnotation()));
                }
            }
        }
        return this;
    }

    @Override
    public AnnotatedStringBuilder append(char c) {
        sb.append(c);
        return this;
    }

    public AnnotatedStringBuilder append(int i) {
        sb.append(i);
        return this;
    }

    public AnnotatedStringBuilder append(CharSequence csq, Annotation annotation) {
        int pos = sb.length();
        if (annotation == null) {
            append(csq);
        } else {
            sb.append(csq);
            if (annotations != null) {
                annotations.add(new AnnotationSpan(pos, sb.length(), annotation));
            }
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

    public void indent(int count) {
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return build().subSequence(start, end);
    }

    public void addAnnotation(int start, int length, Annotation annotation) {
        if (annotations != null) {
            annotations.add(new AnnotationSpan(start, length, annotation));
        }
    }

    @Override
    public Iterable<AnnotationSpan> getAnnotations() {
        return annotations == null ? Collections.<AnnotationSpan>emptyList() : annotations;
    }

    /**
     * Builds an AnnotatedString instance from the contents of this builder.
     */
    public AnnotatedString build() {
        return new AnnotatedString(sb, annotations);
    }

}
