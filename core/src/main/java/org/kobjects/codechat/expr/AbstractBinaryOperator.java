package org.kobjects.codechat.expr;

import org.kobjects.codechat.parser.Parser;

public abstract class AbstractBinaryOperator extends Expression {
    char name;
    Expression left;
    Expression right;

    AbstractBinaryOperator(char name, Expression left, Expression right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    public static int getPrecedence(String name) {
        return (name.length() == 1) ? getPrecedence(name.charAt(0)) : Parser.PRECEDENCE_ASSIGNMENT;
    }

    public static int getPrecedence(char name) {
        switch (name) {
            case '.':
                return Parser.PRECEDENCE_PATH;
            case '^':
            case '\u221a':
                return Parser.PRECEDENCE_POWER;
            case '\u00d7':
            case '/':
            case '%':
                return Parser.PRECEDENCE_MULTIPLICATIVE;
            case '+':
            case '-':
                return Parser.PRECEDENCE_ADDITIVE;

            case '\u2227':
                return Parser.PRECEDENCE_AND;
            case '\u2228':
                return Parser.PRECEDENCE_OR;

            case '=':
            case '<':
            case '>':
            case '\u2260':
            case '\u2264':
            case '\u2266':
                    return Parser.PRECEDENCE_RELATIONAL;

            default:
                throw new RuntimeException("getPrecedence undefined for " + name);
        }
    }

    @Override
    public int getPrecedence() {
        return getPrecedence(name);
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        int precedence = getPrecedence();
        left.toString(sb, 0, precedence);
        sb.append(' ');
        sb.append(name);
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
