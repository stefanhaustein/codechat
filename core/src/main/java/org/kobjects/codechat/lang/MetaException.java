package org.kobjects.codechat.lang;

import java.util.List;
import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public class MetaException extends RuntimeException implements ToAnnotatedString {

    List<? extends Exception> exceptions;

    MetaException(String msg, List<? extends Exception> exceptions) {
        super(msg);
        this.exceptions = exceptions;
    }

    public String toString() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        toString(asb);
        return asb.toString();
    }


    public void toString(AnnotatedStringBuilder asb) {
        asb.append(getMessage()).append('\n');
        for (Exception e : exceptions) {
            asb.append(e.getMessage()).append('\n');
        }
    }
}
