package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.Type;

public class CompoundAssignment extends Expression {
    final char name;
    final Expression left;
    final Expression right;

    public CompoundAssignment(char name, Expression left, Expression right) {
        if (!left.isAssignable()) {
            throw new RuntimeException("Left argument not assignable.");
        }
        if (left.getType() != Type.NUMBER || right.getType() != Type.NUMBER) {
            throw new IllegalArgumentException("Argumnets of '" + name + "=' must be of type Number.");
        }
        this.name = name;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(EvaluationContext context) {
        double r = ((Number) right.eval(context)).doubleValue();
        synchronized (left.getLock(context)) {
            double l = ((Number) left.eval(context)).doubleValue();

            switch(name) {
                case '+':
                    l += r;
                    break;
                case '-':
                    l -= r;
                    break;
                case '\u00d7':
                    l *= r;
                    break;
                case '/':
                    l /= r;
                    break;
                default:
                    throw new RuntimeException("Unsupported compound operator: " + name + "=");

            }

            left.assign(context, l);
            return l;
        }
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_ASSIGNMENT;
    }

    @Override
    public int getChildCount() {
        return 2;
    }


    @Override
    public Type getType() {
        return left.getType();
    }


    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        int precedence = getPrecedence();
        left.toString(sb, indent, precedence);
        sb.append(' ');
        sb.append(name);
        sb.append("=  ");
        right.toString(sb, indent, precedence);
    }


    @Override
    public Expression getChild(int i) {
        return i == 0 ? left : right;
    }

    @Override
    public Expression reconstruct(Expression... children) {
        return new CompoundAssignment(name, children[0], children[1]);
    }


}
