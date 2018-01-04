package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public interface Printable {
    enum Flavor {
      DEFAULT, EDIT, LIST, SAVE, SAVE2, DOCUMENTATION;

    }
    void print(AnnotatedStringBuilder asb, Flavor flavor);
}
