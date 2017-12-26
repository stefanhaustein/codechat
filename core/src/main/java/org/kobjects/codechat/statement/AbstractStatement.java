package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public abstract class AbstractStatement implements Statement {

    final static Object KEEP_GOING = new Object();

    public String toString() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
        toString(asb, 0);
        return asb.toString();
    }

}
