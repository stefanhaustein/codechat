package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.LiteralExpr;
import org.kobjects.codechat.expr.MethodAccess;
import org.kobjects.codechat.expr.PropertyAccessExpr;
import org.kobjects.codechat.instance.Method;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedPath extends UnresolvedExpression {

    UnresolvedExpression base;
    String propertyName;

    public UnresolvedPath(UnresolvedExpression base, String name) {
        super(base.start, base.end + name.length() + 1);
        this.base = base;
        this.propertyName = name;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        Expression resolved = base.resolve(parsingContext, null);
        Type baseType = resolved.getType();
        if (baseType.getType() instanceof EnumType) {
            return new LiteralExpr(((EnumType) baseType.getType()).getValue(propertyName));
        }
        if (baseType instanceof Classifier) {
            Classifier instanceType = (Classifier) baseType;
            if (instanceType.hasProperty(propertyName)) {
                Classifier.PropertyDescriptor property = instanceType.getProperty(propertyName);
                return new PropertyAccessExpr(resolved, property);
            }
            if (instanceType.hasMethod(propertyName)) {
                Method method = instanceType.getMethod(propertyName);
                return new MethodAccess(resolved, method);
            }
            throw new ExpressionParser.ParsingException(start, end, "Method or property '" + propertyName + "' not found in " + baseType, null);
        }
        throw new ExpressionParser.ParsingException(this.base.start, end, "Base type must be tuple type or Enum metatype, but was: " + baseType, null);
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        base.toString(sb, indent);
        sb.append('.');
        sb.append(propertyName);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }
}
