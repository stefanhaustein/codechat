package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.IfStatement;
import org.kobjects.codechat.type.Type;

public class UnresolvedIfStatement extends UnresolvedStatement {

    public UnresolvedExpression condition;
    public UnresolvedStatement ifBody;
    public UnresolvedStatement elseBody;

    public UnresolvedIfStatement(UnresolvedExpression condition, UnresolvedStatement ifBody, UnresolvedStatement elseBody) {
        this.condition = condition;
        this.ifBody = ifBody;
        this.elseBody = elseBody;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        toString(sb, indent, false);
    }

    @Override
    public IfStatement resolve(ParsingContext parsingContext) {
        return new IfStatement(condition.resolve(parsingContext, Type.BOOLEAN), ifBody.resolve(parsingContext), elseBody == null ? null : elseBody.resolve(parsingContext));
    }


    public void toString(AnnotatedStringBuilder sb, int indent, boolean elseif) {
        if (elseif) {
            sb.append("elseif ");
        } else {
            AbstractStatement.indent(sb, indent);
            sb.append("if ");
        }
        condition.toString(sb, indent);
        sb.append(":\n");
        ifBody.toString(sb, indent + 2);
        AbstractStatement.indent(sb, indent);
        if (elseBody instanceof UnresolvedIfStatement) {
            ((UnresolvedIfStatement) elseBody).toString(sb, indent, true);
        } else if (elseBody != null) {
            sb.append("else:\n");
            elseBody.toString(sb, indent + 2);
            AbstractStatement.indent(sb, indent);
        }
        sb.append("end\n");
    }
}
