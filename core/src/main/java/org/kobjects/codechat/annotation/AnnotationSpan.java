package org.kobjects.codechat.annotation;

import java.lang.ref.WeakReference;
import java.util.List;

public class AnnotationSpan {
    private final int start;
    private final int end;
    private final Annotation annotation;

    public static void append(StringBuilder sb, String s, Link link, List<AnnotationSpan> list) {
        if (list != null) {
            list.add(new AnnotationSpan(sb.length(), sb.length() + s.length(), link));
        }
        sb.append(s);
    }

    public AnnotationSpan(int start, int end, Annotation annotation) {
        this.start = start;
        this.end = end;
        this.annotation = annotation;
    }

    public String toString() {
        return "[" + start + ":" + end + "]:" + annotation;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Link getLink() {
        return annotation instanceof Link ? (Link) annotation : null;
    }

    public Annotation getAnnotation() {
        return annotation;
    }
}
