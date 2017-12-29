package org.kobjects.codechat.lang;

import java.util.List;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public class MetaException extends RuntimeException implements Printable {

    List<? extends Exception> exceptions;

    MetaException(String msg, List<? extends Exception> exceptions) {
        super(msg);
        this.exceptions = exceptions;
    }

    public String toString() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        print(asb, Flavor.DEFAULT);
        return asb.toString();
    }


    public void print(AnnotatedStringBuilder asb, Printable.Flavor flavor) {
        asb.append(getMessage()).append('\n');
        for (Exception e : exceptions) {
            asb.append(e.getMessage()).append('\n');
        }
    }
}
