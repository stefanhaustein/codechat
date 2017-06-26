package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.BinaryOperator;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

public class UnresolvedBinaryOperator extends UnresolvedExpression {
    public char name;
    public UnresolvedExpression left;
    public UnresolvedExpression right;

    public UnresolvedBinaryOperator(char name, UnresolvedExpression left, UnresolvedExpression right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    public int getPrecedence() {
        return BinaryOperator.getPrecedence(name);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        Expression left = this.left.resolve(parsingContext);
        Expression right = this.right.resolve(parsingContext);
        if (!left.getType().equals(right.getType())) {
            throw new RuntimeException("Argument types must match for operator '" + name + "'");
        }

        if (left.getType().equals(Type.STRING)) {
            if (name != '+') {
                throw new RuntimeException("Operator '" + name + "' not defined for strings");
            }
            name = '$';
        } else if (name == '\u2227' || name == '\u2228') {
            if (!left.getType().equals(Type.BOOLEAN)) {
                throw new RuntimeException("Arguments must be boolean for operator " + name);
            }
        } else if (!left.getType().equals(Type.NUMBER)) {
            throw new RuntimeException("Arguments must be numbers" + (name == '+' ? " or strings" : "") + " for operator '" + name + "'");
        }
        return new org.kobjects.codechat.expr.BinaryOperator(name, left, right);
    }


    @Override
    public void toString(StringBuilder sb, int indent) {
        int precedence = getPrecedence();
        left.toString(sb, 0, precedence);
        sb.append(' ');
        sb.append(name == '$' ? '+' : name);
        sb.append(' ');
        right.toString(sb, 0, precedence);
    }
}
