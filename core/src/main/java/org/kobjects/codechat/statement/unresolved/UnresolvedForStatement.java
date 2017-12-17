package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.ForStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.CollectionType;
import org.kobjects.codechat.type.Type;

public class UnresolvedForStatement extends UnresolvedStatement {
    String variableName;
    UnresolvedExpression expression;
    UnresolvedStatement body;


    public UnresolvedForStatement(String variableName, UnresolvedExpression expression, UnresolvedStatement body) {
        this.variableName = variableName;
        this.expression = expression;
        this.body = body;
    }
    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("for ").append(variableName).append(" in ");
        expression.toString(sb, 0);
        sb.append(":\n");

        body.toString(sb, indent + 2);

        AbstractStatement.indent(sb, indent);
        sb.append("end\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        Expression resolved = expression.resolve(parsingContext, null);

        if (!(resolved.getType() instanceof CollectionType)) {
            throw new RuntimeException("For expression must be a list.");
        }
        Type elementType = ((CollectionType) resolved.getType()).elementType;

        ParsingContext foreachParsingContext = new ParsingContext(parsingContext, false);

        LocalVariable counter = foreachParsingContext.addVariable(variableName, elementType, true);

        return new ForStatement(counter, resolved, body.resolve(foreachParsingContext));
    }
}
