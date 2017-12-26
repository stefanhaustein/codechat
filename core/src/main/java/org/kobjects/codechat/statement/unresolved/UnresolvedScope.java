package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Scope;
import org.kobjects.codechat.statement.Statement;

public class UnresolvedScope extends UnresolvedStatement {

    private final UnresolvedStatement body;

    public UnresolvedScope(UnresolvedStatement body) {
        this.body = body;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      sb.append("scope:\n");
        body.toString(sb, indent + 2);
      sb.indent(indent);
      sb.append("end\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        return new Scope(body.resolve(new ParsingContext(parsingContext, false)));
    }
}
