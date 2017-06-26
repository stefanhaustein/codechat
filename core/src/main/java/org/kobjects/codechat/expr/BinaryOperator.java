package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

public class BinaryOperator extends Expression {
    public char name;
    public Expression left;
    public Expression right;

    public BinaryOperator(char name, Expression left, Expression right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(EvaluationContext context) {
        switch(name) {
            case '$':
                return (String) left.eval(context) + (String) right.eval(context);
            case '\u2227':
                return Boolean.TRUE.equals(left.eval(context)) ? right.eval(context) : Boolean.FALSE;
            case '\u2228':
                return Boolean.FALSE.equals(left.eval(context)) ? right.eval(context) : Boolean.TRUE;
        }
        double l = ((Number) left.eval(context)).doubleValue();
        double r = ((Number) right.eval(context)).doubleValue();

        switch (name) {
            case '+': return l + r;
            case '-': return l - r;
            case '*': return l * r;
            case '/': return l / r;
            case '^': return Math.pow(l, r);
            case '\u221a': return l == 2 ? Math.sqrt(r) : Math.pow(r, 1/l);
            default:
                throw new RuntimeException("Impossible");
        }
    }

    public static int getPrecedence(char name) {
        switch (name) {
            case '\u2227':
                return Parser.PRECEDENCE_AND;
            case '\u2228':
                return Parser.PRECEDENCE_OR;
            case '^':
            case '\u221a':
                return Parser.PRECEDENCE_POWER;
            case '*':
            case '/':
                return Parser.PRECEDENCE_MULTIPLICATIVE;
            case '$':
            case '+':
            case '-':
                return Parser.PRECEDENCE_ADDITIVE;

            default:
                throw new RuntimeException("getPrecedence undefined for " + name);

        }
    }

    @Override
    public int getPrecedence() {
        return getPrecedence(name);
    }

    @Override
    public Type getType() {
        return left.getType();
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

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public Expression getChild(int i) {
        return i == 0 ? left : right;
    }
}
