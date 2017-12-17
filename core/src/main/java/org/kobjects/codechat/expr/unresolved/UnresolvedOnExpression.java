package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.statement.unresolved.UnresolvedStatement;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedOnExpression extends UnresolvedExpression {

    private final OnInstance.OnInstanceType type;
    private final int id;
    private final UnresolvedExpression expression;
    private final UnresolvedStatement body;

    public UnresolvedOnExpression(int start, int end, OnInstance.OnInstanceType type, int id, UnresolvedExpression expression, UnresolvedStatement body) {
        super(start, end);
        this.id = id;
        this.type = type;
        this.expression = expression;
        this.body = body;
    }

    @Override
    public OnExpression resolve(ParsingContext parsingContext, Type expectedType) {
        ParsingContext closureParsingContext = new ParsingContext(parsingContext, true);

        Expression resolved = expression.resolve(closureParsingContext, null);

        if (type == OnInstance.ON_CHANGE_TYPE) {
            if (!(resolved instanceof PropertyAccess)) {
                throw new ExpressionParser.ParsingException(expression.start, expression.end, "property expected.", null);
            }
        } else if (type == OnInstance.ON_TYPE) {
            if (!resolved.getType().equals(Type.BOOLEAN)) {
                throw new ExpressionParser.ParsingException(expression.start, expression.end, "Boolean expression expected.", null);
            }
        } else if (type == OnInstance.ON_INTERVAL_TYPE) {
            if (!resolved.getType().equals(Type.NUMBER)) {
                throw new ExpressionParser.ParsingException(expression.start, expression.end, "Boolean expression expected.", null);
            }
        } else {
            throw new IllegalArgumentException();
        }

        Statement resolvedBody = body.resolve(closureParsingContext);

        return new OnExpression(type, id, resolved, resolvedBody, closureParsingContext.getClosure());
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append(type.getName().toLowerCase());
        if (id != -1) {
            sb.append('#').append(id);
        }
        sb.append(' ');
        expression.toString(sb, indent);
        sb.append(":\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("end");
    }

    @Override
    public int getPrecedence() {
        return 0;
    }
}
