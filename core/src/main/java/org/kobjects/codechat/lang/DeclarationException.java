package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;

public class DeclarationException extends RuntimeException implements Printable {

    final RootVariable variable;

    public DeclarationException(RootVariable variable, Exception cause) {
        super(cause);
        this.variable = variable;
    }

    public String toString() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        toString(asb);
        return asb.toString();
    }

    @Override
    public void toString(AnnotatedStringBuilder asb) {
        asb.append("Error in declaration of '");
        asb.append(variable.name, new EntityLink(variable));
        if (getCause() == null) {
            asb.append("'.");
        } else {
            asb.append("': ");
            Formatting.exceptionToString(asb, getCause());
        }
    }
}
