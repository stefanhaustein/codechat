package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.CountStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedCountStatement extends UnresolvedStatement {
    String variableName;
    UnresolvedExpression expression;
    UnresolvedStatement body;


    public UnresolvedCountStatement(String variableName, UnresolvedExpression expression, UnresolvedStatement body) {
        this.variableName = variableName;
        this.expression = expression;
        this.body = body;
    }


    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("count ").append(variableName).append(" to ");
        expression.toString(sb, 0);
        sb.append(":\n");

        body.toString(sb, indent + 2);

        AbstractStatement.indent(sb, indent);
        sb.append("end\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        ParsingContext countParsingContext = new ParsingContext(parsingContext, false);

        LocalVariable counter = countParsingContext.addVariable(variableName, Type.NUMBER, true);

        Expression resolvedExpression = expression.resolve(parsingContext, Type.NUMBER);
        if (!resolvedExpression.getType().equals(Type.NUMBER)) {
            throw new ExpressionParser.ParsingException(expression.start, expression.end, "Count expression must be a number.", null);
        }

        return new CountStatement(counter, expression.resolve(parsingContext, Type.NUMBER), body.resolve(countParsingContext));
    }
}
