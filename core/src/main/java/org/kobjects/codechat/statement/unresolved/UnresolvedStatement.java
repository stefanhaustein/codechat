package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Statement;

public abstract class UnresolvedStatement {

    public abstract void toString(AnnotatedStringBuilder sb, int indent);

    public abstract Statement resolve(ParsingContext parsingContext);

    public void resolveTypes(ParsingContext parsingContext) {
    }

    public final String toString() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
        toString(asb, 0);
        return asb.toString();
    }
}