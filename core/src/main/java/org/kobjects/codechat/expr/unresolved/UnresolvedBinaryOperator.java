package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.AbstractBinaryOperator;
import org.kobjects.codechat.expr.BinaryLogicalOperator;
import org.kobjects.codechat.expr.BinaryMathOperator;
import org.kobjects.codechat.expr.CompoundAssignment;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.expr.RelationalOperator;
import org.kobjects.codechat.expr.StringConcatenation;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedBinaryOperator extends UnresolvedExpression {
    public String name;
    public UnresolvedExpression left;
    public UnresolvedExpression right;

    public UnresolvedBinaryOperator(String name, UnresolvedExpression left, UnresolvedExpression right) {
        super(left.end, right.start);
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
        if (".".equals(name)) {
            if (!(right instanceof UnresolvedIdentifier)) {
                throw new ExpressionParser.ParsingException(this.left.start, end, "Identifer expected for dot operator", null);
            }
            String propertyName = ((UnresolvedIdentifier) right).name;
            Type type = left.getType();
            if (type.getType() instanceof EnumType) {
                return new Literal(((EnumType) type.getType()).getValue(propertyName));
            } else if (type instanceof InstanceType) {
                InstanceType instanceType = (InstanceType) left.getType();
                InstanceType.PropertyDescriptor property = instanceType.getProperty(propertyName);
                return new PropertyAccess(left, property);
            } else {
                throw new ExpressionParser.ParsingException(this.left.start, end, "Base type must be tuple type or Enum metatype, but was: " + type, null);
            }
        }
        Expression right = this.right.resolve(parsingContext, left.getType());

        try {
            if (left.getType() == Type.STRING && "+".equals(name)) {
                return new StringConcatenation(left, right);
            }

            if (name.length() != 1) {
                if (name.endsWith("=")) {
                    return new CompoundAssignment(name.charAt(0), left, right);
                }
                throw new RuntimeException("Unrecognized operator: '" + name +"'");
            }

            switch (name.charAt(0)) {
                case '\u2227':
                case '\u2228':
                        return new BinaryLogicalOperator(name.charAt(0), left, right);

                    case '=':
                    case '<':
                    case '>':
                    case '\u2260':
                    case '\u2264':
                    case '\u2266':
                        return new RelationalOperator(name.charAt(0), left, right);
                default:
                    return new BinaryMathOperator(name.charAt(0), left, right);
            }
        } catch (Exception e) {
            throw new ExpressionParser.ParsingException(this.start, this.end, e.getMessage(), e);
        }
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        int precedence = getPrecedence();
        left.toString(sb, 0, precedence);
        sb.append(' ');
        switch(name) {
            case "∧":
                sb.append("and");
                break;
            case "∨":
                sb.append("or");
                break;
            default:
                sb.append(name);
        }
        sb.append(' ');
        right.toString(sb, 0, precedence);
    }
}
