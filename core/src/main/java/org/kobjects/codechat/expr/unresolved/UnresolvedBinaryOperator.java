package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.AbstractBinaryOperator;
import org.kobjects.codechat.expr.BinaryLogicalOperator;
import org.kobjects.codechat.expr.BinaryMathOperator;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.expr.RelationalOperator;
import org.kobjects.codechat.expr.StringConcatenation;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.TupleType;
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
        return AbstractBinaryOperator.getPrecedence(name);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        Expression left = this.left.resolve(parsingContext, null);
        if (name == '.') {
            if (!(right instanceof UnresolvedIdentifier)) {
                throw new RuntimeException("Identifer expected for dot operator");
            }
            String propertyName = ((UnresolvedIdentifier) right).name;
            Type type = left.getType();
            if (type.getType() instanceof EnumType) {
                return new Literal(((EnumType) type.getType()).getValue(propertyName));
            } else if (type instanceof TupleType) {
                TupleType instanceType = (TupleType) left.getType();
                TupleType.PropertyDescriptor property = instanceType.getProperty(propertyName);
                return new PropertyAccess(left, property);
            } else {
                throw new RuntimeException("Base type must be tuple type or Enum metatype, but was: " + type);
            }
        }
        Expression right = this.right.resolve(parsingContext, null);

        if (left.getType() == Type.STRING && name == '+') {
            return new StringConcatenation(left, right);
        }

        switch (name) {
            case '\u2227':
            case '\u2228':
                return new BinaryLogicalOperator(name, left, right);

            case '=':
            case '<':
            case '>':
            case '\u2260':
            case '\u2264':
            case '\u2266':
                return new RelationalOperator(name, left, right);

            case '+':
                if (left.getType() == Type.STRING) {
                    return new StringConcatenation(left, right);
                }
                // Fallthrough intended
            default:
                return new BinaryMathOperator(name, left, right);
        }

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
