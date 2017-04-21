package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.kobjects.codechat.api.Builtins;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class UnresolvedInvocation extends AbstractUnresolved {

    static int getPrecedence(boolean parens) {
        return  parens ? Parser.PRECEDENCE_PATH : Parser.PRECEDENCE_IMPLICIT;
    }

    static void toString(StringBuilder sb, String name, boolean parens, Expression[] children) {
        if (parens) {
            sb.append(name);
            sb.append('(');
            if (children.length > 0) {
                children[0].toString(sb, 0);
                for (int i = 1; i < children.length; i++) {
                    sb.append(", ");
                    children[0].toString(sb, 0);
                }
            }
            sb.append(')');
        } else {
            sb.append(name);
            for (int i = 0; i < children.length; i++) {
                sb.append(' ');
                children[i].toString(sb, Parser.PRECEDENCE_PATH);
            }
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
    public Expression resolve(Scope scope) {
        if ("create".equals(name) && children[1] instanceof Identifier) {
            String argName = ((Identifier) children[0]).name;

            Type type = scope.environment.resolveType(argName);
            if (type != null && Instance.class.isAssignableFrom(type.getJavaClass())) {
                return new ConstructorInvocation(type);
            }
        }

        Expression[] resolved = new Expression[children.length];
        for (int i = 0; i < resolved.length; i++) {
            resolved[i] = children[i].resolve(scope);
        }

        if (Instance.class.isAssignableFrom(resolved[0].getType().getJavaClass())) {
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
        } else {
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
    }

    @Override
    public int getPrecedence() {
        return getPrecedence(parens);
    }

    @Override
    public void toString(StringBuilder sb) {
        toString(sb, name, parens, children);
    }
}
