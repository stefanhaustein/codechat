package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.RelationalOperator;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.type.Type;

public class UnresolvedRelationalOperator extends UnresolvedExpression {

    public char name;

    public UnresolvedExpression left;
    public UnresolvedExpression right;

    public UnresolvedRelationalOperator(char name, UnresolvedExpression left, UnresolvedExpression right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        Expression left = this.left.resolve(parsingContext);
        Expression right = this.right.resolve(parsingContext);
        return new RelationalOperator(name, left, right);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_RELATIONAL;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        left.toString(sb, 0, Parser.PRECEDENCE_RELATIONAL);
        sb.append(' ');
        sb.append(name);
        sb.append(' ');
        right.toString(sb, 0, Parser.PRECEDENCE_RELATIONAL);
    }
}
