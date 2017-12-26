package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.DeleteStatement;
import org.kobjects.codechat.statement.ReturnStatement;
import org.kobjects.codechat.statement.Statement;

public class UnresolvedSimpleStatement extends UnresolvedStatement {
    
    public enum Kind {
        RETURN, DELETE
    }
    
    private Kind kind;
    private UnresolvedExpression expression;
    
    public UnresolvedSimpleStatement(Kind kind, UnresolvedExpression expression) {
        this.kind = kind;
        this.expression = expression;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      sb.append(kind.toString().toLowerCase()).append(' ');
        expression.toString(sb, indent + 4);
        sb.append("\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        Expression resolved = expression.resolve(parsingContext, null);

        switch (kind) {
            case RETURN:
                return new ReturnStatement(resolved);
            case DELETE:
                return new DeleteStatement(resolved, parsingContext);
            default:
                throw new RuntimeException();
        }

    }
}
