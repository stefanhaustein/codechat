package org.kobjects.codechat.lang;

import java.lang.ref.WeakReference;
import java.util.List;

public class AnnotationSpan {
    private final int start;
    private final int end;
    private final WeakReference<Object> link;

    public static void append(StringBuilder sb, String s, Object link, List<AnnotationSpan> list) {
        if (list != null) {
            list.add(new AnnotationSpan(sb.length(), sb.length() + s.length(), link));
        }
        sb.append(s);
    }

    public AnnotationSpan(int start, int end, Object link) {
        this.start = start;
        this.end = end;
        this.link = new WeakReference<Object>(link);
    }

    public String toString() {
        return "[" + start + ":" + end + "]:" + link;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Object getLink() {
        return link.get();
    }
}
