package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public interface Printable {
    enum Flavor {
      DEFAULT

    }
    void print(AnnotatedStringBuilder asb, Flavor flavor);
}
