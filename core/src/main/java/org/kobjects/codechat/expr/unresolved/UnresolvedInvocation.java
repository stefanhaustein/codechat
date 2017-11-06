package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.CollectionLiteral;
import org.kobjects.codechat.expr.ConstructorInvocation;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionInvocation;
import org.kobjects.codechat.expr.InstanceReference;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedInvocation extends UnresolvedExpression {

    static void toString(AnnotatedStringBuilder sb, UnresolvedExpression base, boolean parens, UnresolvedExpression[] children) {
        base.toString(sb, 0);
        sb.append(parens ? '(' : ' ');
        if (children.length > 0) {
            children[0].toString(sb, 0, 0);
            for (int i = 1; i < children.length; i++) {
                sb.append(", ");
                children[i].toString(sb, 0, 0);
            }
        }
        if (parens) {
            sb.append(')');
        }
    }


    public UnresolvedExpression base;
    public UnresolvedExpression[] children;
    public boolean parens;

    public UnresolvedInvocation(int end, UnresolvedExpression base, boolean parens, UnresolvedExpression... children) {
        super(base.start, end);
        this.base = base;
        this.parens = parens;
        this.children = children;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        if (base instanceof UnresolvedIdentifier) {
            String name = ((UnresolvedIdentifier) base).name;

            if (("create".equals(name) || "new".equals(name)) && children[0] instanceof UnresolvedIdentifier) {
                String argName = ((UnresolvedIdentifier) children[0]).name;

                Type type = parsingContext.environment.resolveType(argName);
                return new ConstructorInvocation(type, -1);
            }
            if ("new".equals(name) && children[0] instanceof UnresolvedInstanceReference) {
                InstanceReference resolvedRef = (InstanceReference) children[0].resolve(parsingContext, null);
                return new ConstructorInvocation(resolvedRef.type, resolvedRef.id);
            }
        }

        Expression[] resolved = new Expression[children.length];
        Type[] paramTypes = new Type[resolved.length];
        for (int i = 0; i < resolved.length; i++) {
            resolved[i] = children[i].resolve(parsingContext, null);
            paramTypes[i] = resolved[i].getType();
        }

        if (base instanceof UnresolvedIdentifier) {
            try {
                String name = ((UnresolvedIdentifier) base).name;
                if ("Set".equals(name)) {
                    return new CollectionLiteral(SetType.class, resolved);
                }
                if ("List".equals(name)) {
                    return new CollectionLiteral(ListType.class, resolved);
                }
            } catch (Exception e) {
                throw new ExpressionParser.ParsingException(start, end, e.getMessage(), e);
            }
        }

        Expression resolvedBase = base.resolve(parsingContext, null);

        /*
        if (resolvedBase instanceof RootVariableNode) {
            int best = 0;
            Function result = null;
            for (Function candidate: ((RootVariableNode) resolvedBase).rootVariable.functions()) {
                double match = candidate.getType().callScore(paramTypes);
                if (match > best) {
                    result = candidate;
                }
            }
            if (result != null) {
                return new NamedFunctionInvocation(((RootVariableNode) resolvedBase).rootVariable.name, result, parens, resolved);
            }
        }
        */

        if (!(resolvedBase.getType() instanceof FunctionType)) {
            throw new ExpressionParser.ParsingException(start, end, "Not a function: " + resolvedBase + ": " + resolvedBase.getType() + " / " + resolvedBase.getClass(), null);
        }

        FunctionType functionType = (FunctionType) resolvedBase.getType();

        if (functionType.parameterTypes.length != resolved.length) {
            // FIXME
            if (base instanceof UnresolvedIdentifier && base.toString().equals("help")
                    && functionType.parameterTypes.length == 1 && resolved.length == 0) {
                // ok
            } else {
                throw new ExpressionParser.ParsingException(start, end, "UserFunction argument count mismatch. Expected: "
                        + functionType.parameterTypes.length + " actual: " + resolved.length, null);
            }
        }

        for (int i = 0; i < resolved.length; i++) {
            if (!functionType.parameterTypes[i].isAssignableFrom(resolved[i].getType())) {
                throw new ExpressionParser.ParsingException(start, end, "Type mismatch for paramerer " + i + "; expected: " + functionType.parameterTypes[i] + " actual: " + resolved[i].getType(), null);
            }
        }

        return new FunctionInvocation(resolvedBase, parens, resolved);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        toString(sb, base, parens, children);
    }

}
