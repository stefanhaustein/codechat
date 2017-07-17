package org.kobjects.codechat.lang;

import java.util.List;

public class SimpleDocumented implements Documented {
    private final String s;

    public SimpleDocumented(String s) {
        this.s = s;
    }

    @Override
    public String getDocumentation(List<Annotation> links) {
        return s;
    }
}
