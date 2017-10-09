package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.Type;

public class UnresolvedLiteral extends UnresolvedExpression {
    final Object value;

    public UnresolvedLiteral(int start, int end, Object value) {
        super(start, end);
        this.value = value;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        return new Literal(value);
    }

    @Override
    public int getPrecedence() {
        return value instanceof Number && ((Number) value).doubleValue() < 0 ? Parser.PRECEDENCE_SIGN : Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, int indent) {
        Formatting.toLiteral(asb, value);
    }

}
