package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.kobjects.codechat.lang.Builtins;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;

public class UnresolvedInvocation extends AbstractUnresolved {

    static int getPrecedence(boolean parens) {
        return  parens ? Parser.PRECEDENCE_PATH : Parser.PRECEDENCE_IMPLICIT;
    }

    static void toString(StringBuilder sb, String name, boolean parens, Expression[] children) {
        sb.append(name);
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


    public String name;
    public Expression[] children;
    public boolean parens;

    public UnresolvedInvocation(String name, boolean parens, Expression... children) {
        this.name = name;
        this.parens = parens;
        this.children = children;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        if (("create".equals(name)  || "new".equals(name)) && children[0] instanceof Identifier) {
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

        Expression[] resolved = new Expression[children.length];
        for (int i = 0; i < resolved.length; i++) {
            resolved[i] = children[i].resolve(parsingContext);
        }

        Type type = resolved[0].getType();
        if (Type.NUMBER.equals(type) || Type.BOOLEAN.equals(type) || Type.STRING.equals(type)) {
            Class[] paramTypes = new Class[resolved.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = resolved[i].getType().getJavaClassForSignature();
            }
            Method method;
            try {
                method = Builtins.class.getMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                try {
                    method = Math.class.getMethod(name, paramTypes);
                } catch (NoSuchMethodException e2) {
                    throw new RuntimeException("Method '" + name + "' with parameter types " + Arrays.toString(Arrays.copyOfRange(paramTypes, 1, paramTypes.length)) + " not found in class " + resolved[0].getType());
                }
            }
            return new BuiltinInvocation(method, false, resolved);
        }
        Class[] paramTypes = new Class[resolved.length - 1];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = resolved[i + 1].getType().getJavaClassForSignature();
        }
        try {
            Method method = resolved[0].getType().getJavaClass().getMethod(name, paramTypes);
            return new MethodInvocation(method, parens, resolved);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method '" + name + "' with parameter types " + Arrays.toString(paramTypes) + " not found in class " + resolved[0].getType());
        }
    }

    @Override
    public int getPrecedence() {
        return getPrecedence(parens);
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        toString(sb, name, parens, children);
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
