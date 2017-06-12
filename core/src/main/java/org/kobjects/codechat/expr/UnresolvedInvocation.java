package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

public class UnresolvedInvocation extends AbstractUnresolved {

    static int getPrecedence(boolean parens) {
        return  parens ? Parser.PRECEDENCE_PATH : Parser.PRECEDENCE_IMPLICIT;
    }

    static void toString(StringBuilder sb, Expression base, boolean parens, Expression[] children) {
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


    public Expression base;
    public Expression[] children;
    public boolean parens;

    public UnresolvedInvocation(Expression base, boolean parens, Expression... children) {
        this.base = base;
        this.parens = parens;
        this.children = children;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {

        Expression[] resolved = new Expression[children.length];
        if (base instanceof Identifier) {
            String name = ((Identifier) base).name;

            if (("create".equals(name) || "new".equals(name)) && children[0] instanceof Identifier) {
                String argName = ((Identifier) children[0]).name;

                Type type = parsingContext.environment.resolveType(argName);
                if (type != null && Instance.class.isAssignableFrom(type.getJavaClass())) {
                    return new ConstructorInvocation(type, -1);
                }
            }
            if ("new".equals(name) && children[0] instanceof InstanceReference) {
                InstanceReference resolvedRef = (InstanceReference) children[0].resolve(parsingContext);
                return new ConstructorInvocation(resolvedRef.type, resolvedRef.id);
            }

            Type[] paramTypes = new Type[resolved.length];
            for (int i = 0; i < resolved.length; i++) {
                resolved[i] = children[i].resolve(parsingContext);
                paramTypes[i] = resolved[i].getType();
            }

            if ("set".equals(name)) {
                return new CollectionLiteral(SetType.class, resolved);
            }
            if ("list".equals(name)) {
                return new CollectionLiteral(ListType.class, resolved);
            }

            for (Object builtins : parsingContext.environment.builtins) {
                Class<?> builtinClass = builtins instanceof Class ? (Class) builtins : builtins.getClass();
                Class[] paramJavaTypes = new Class[resolved.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    paramJavaTypes[i] = Type.getJavaClassForSignature(resolved[i].getType());
                }
                try {
                    Method method = builtinClass.getMethod(name, paramJavaTypes);
                    return new BuiltinInvocation(builtins instanceof Class ? null : builtins, method, parens, resolved);
                } catch (NoSuchMethodException e) {
                }
            }

            String qualifiedName = FunctionExpression.getQualifiedName(name, paramTypes);
            RootVariable fVar = parsingContext.environment.rootVariables.get(qualifiedName);
            if (fVar != null && fVar.type instanceof FunctionType) {
                return new FunctionInvocation(new RootVariableNode(qualifiedName, fVar.type), resolved);
            }

        } else {
            for (int i = 0; i < resolved.length; i++) {
                resolved[i] = children[i].resolve(parsingContext);
            }
        }


        Expression resolvedBase = base.resolve(parsingContext);

        if (resolvedBase.getType() instanceof MetaType) {
            Type baseType = ((MetaType)resolvedBase.getType()).getType();
            if (baseType instanceof TupleType && resolved.length == 0) {
                return new ObjectLiteral(base, new LinkedHashMap<String, Expression>()).resolve(parsingContext);
            }
        }

        if (!(resolvedBase.getType() instanceof FunctionType)) {
            throw new RuntimeException("Not a function: " + resolvedBase + ": " + resolvedBase.getType() + " / " + resolvedBase.getType().getClass());
        }

        FunctionType functionType = (FunctionType) resolvedBase.getType();

        if (functionType.parameterTypes.length != resolved.length) {
            throw new RuntimeException("UserFunction argument count mismatch.");
        }

        for (int i = 0; i < resolved.length; i++) {
            if (!functionType.parameterTypes[i].isAssignableFrom(resolved[i].getType())) {
                throw new RuntimeException("Type mismatch for paramerer " + i + "; expected: " + functionType.parameterTypes[i] + " actual: " + resolved[i].getType());
            }
        }

        return new FunctionInvocation(resolvedBase, resolved);
    }

    @Override
    public int getPrecedence() {
        return getPrecedence(parens);
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        toString(sb, base, parens, children);
    }

    @Override
    public int getChildCount() {
        return children.length;
    }

    @Override
    public Expression getChild(int index) {
        return children[index];
    }
}
