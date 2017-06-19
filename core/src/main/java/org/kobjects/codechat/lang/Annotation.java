package org.kobjects.codechat.lang;

import java.util.List;

public class Annotation {
    public final int start;
    public final int end;
    public final Object link;

    static void append(StringBuilder sb, String s, Object link, List<Annotation> list) {
        if (list != null) {
            list.add(new Annotation(sb.length(), sb.length() + s.length(), link));
        }
        sb.append(s);
    }

    public Annotation(int start, int end, Object link) {
        this.start = start;
        this.end = end;
        this.link = link;
    }

    public String toString() {
        return "[" + start + ":" + end + "]:" + link;
    }
}
