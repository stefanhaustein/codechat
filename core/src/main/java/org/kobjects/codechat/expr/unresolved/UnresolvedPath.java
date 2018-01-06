package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.MethodAccess;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.lang.UserMethod;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.InstanceType;
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
            return new Literal(((EnumType) baseType.getType()).getValue(propertyName));
        }
        if (baseType instanceof InstanceType) {
            InstanceType instanceType = (InstanceType) baseType;
            if (instanceType.hasProperty(propertyName)) {
                InstanceType.PropertyDescriptor property = instanceType.getProperty(propertyName);
                return new PropertyAccess(resolved, property);
            }
            if (instanceType.hasMethod(propertyName)) {
                UserMethod method = instanceType.getMethod(propertyName);
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
