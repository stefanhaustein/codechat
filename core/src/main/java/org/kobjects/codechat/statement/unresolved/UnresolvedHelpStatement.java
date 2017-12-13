package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.HelpStatement;

public class UnresolvedHelpStatement extends UnresolvedStatement {

    private final String about;

    public UnresolvedHelpStatement(String about) {
        this.about = about;
    }



    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append("help");
        if (about != null) {
            sb.append(' ').append(about);
        }
    }

    @Override
    public HelpStatement resolve(ParsingContext parsingContext) {
        return new HelpStatement(about);
    }

}
