package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.ConstructorInvocation;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedConstructor;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedMultiAssignment;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.DeleteStatement;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.ReturnStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

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
        AbstractStatement.indent(sb, indent);
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
