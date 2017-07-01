package org.kobjects.codechat.expr.unresolved;

import java.util.LinkedHashMap;

import org.kobjects.codechat.expr.CollectionLiteral;
import org.kobjects.codechat.expr.ConstructorInvocation;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionInvocation;
import org.kobjects.codechat.expr.InstanceReference;
import org.kobjects.codechat.expr.ObjectLiteral;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

public class UnresolvedInvocation extends UnresolvedExpression {

    static void toString(StringBuilder sb, UnresolvedExpression base, boolean parens, UnresolvedExpression[] children) {
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

    public UnresolvedInvocation(UnresolvedExpression base, boolean parens, UnresolvedExpression... children) {
        this.base = base;
        this.parens = parens;
        this.children = children;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        if (base instanceof UnresolvedIdentifier) {
            String name = ((UnresolvedIdentifier) base).name;

            if (("create".equals(name) || "new".equals(name)) && children[0] instanceof UnresolvedIdentifier) {
                String argName = ((UnresolvedIdentifier) children[0]).name;

                Type type = parsingContext.environment.resolveType(argName);
                return new ConstructorInvocation(type, -1);
            }
            if ("new".equals(name) && children[0] instanceof UnresolvedInstanceReference) {
                InstanceReference resolvedRef = (InstanceReference) children[0].resolve(parsingContext);
                return new ConstructorInvocation(resolvedRef.type, resolvedRef.id);
            }
        }

        Expression[] resolved = new Expression[children.length];
        Type[] paramTypes = new Type[resolved.length];
        for (int i = 0; i < resolved.length; i++) {
            resolved[i] = children[i].resolve(parsingContext);
            paramTypes[i] = resolved[i].getType();
        }

        if (base instanceof UnresolvedIdentifier) {
            String name = ((UnresolvedIdentifier) base).name;
            if ("Set".equals(name)) {
                return new CollectionLiteral(SetType.class, resolved);
            }
            if ("List".equals(name)) {
                return new CollectionLiteral(ListType.class, resolved);
            }
        }

        Expression resolvedBase = base.resolve(parsingContext);

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

        if (resolvedBase.getType() instanceof MetaType) {
            Type baseType = ((MetaType)resolvedBase.getType()).getType();
            if (baseType instanceof TupleType && resolved.length == 0) {
                return new ObjectLiteral((TupleType) baseType, -1, new LinkedHashMap<String, Expression>());
            }
        }

        if (!(resolvedBase.getType() instanceof FunctionType)) {
            throw new RuntimeException("Not a function: " + resolvedBase + ": " + resolvedBase.getType() + " / " + resolvedBase.getClass());
        }

        FunctionType functionType = (FunctionType) resolvedBase.getType();

        if (functionType.parameterTypes.length != resolved.length) {
            throw new RuntimeException("UserFunction argument count mismatch. Expected: " + functionType.parameterTypes.length + " actual: " + resolved.length);
        }

        for (int i = 0; i < resolved.length; i++) {
            if (!functionType.parameterTypes[i].isAssignableFrom(resolved[i].getType())) {
                throw new RuntimeException("Type mismatch for paramerer " + i + "; expected: " + functionType.parameterTypes[i] + " actual: " + resolved[i].getType());
            }
        }

        return new FunctionInvocation(resolvedBase, parens, resolved);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        toString(sb, base, parens, children);
    }

}
