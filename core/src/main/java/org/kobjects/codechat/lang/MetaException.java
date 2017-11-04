package org.kobjects.codechat.lang;

import java.util.List;
import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public class MetaException extends RuntimeException {

    List<? extends Exception> exceptions;
    Map<Entity, Exception> errors;

    public static void toString(Map<Entity, Exception> errors, AnnotatedStringBuilder asb) {
        for (Map.Entry<Entity, Exception> entry : errors.entrySet()) {
            Formatting.toLiteral(entry.getKey());
            asb.append(": ").append(entry.getValue().getMessage());
        }
    }

    MetaException(String msg, List<? extends Exception> exceptions, Map<Entity,Exception> errors) {
        super(msg);
        this.exceptions = exceptions;
        this.errors = errors;
    }


    public void toString(AnnotatedStringBuilder asb) {
        asb.append(getMessage()).append('\n');
        for (Exception e : exceptions) {
            asb.append(e.getMessage()).append('\n');
        }
        toString(errors, asb);
    }
}
