package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.kobjects.codechat.api.Builtins;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class Implicit extends Unresolved {
    public Expression[] children;
    public Implicit(Expression... children) {
        this.children = children;
    }


    @Override
    public Expression resolve(Scope scope) {
        if (!(children[0] instanceof Identifier)) {
            throw new RuntimeException("verb expected as first parameter");
        }
        String name = ((Identifier) children[0]).name;

        if ("create".equals(name) && children[1] instanceof Identifier) {
            String argName = ((Identifier) children[1]).name;

            Type type = scope.environment.resolveType(argName);
            if (type != null && Instance.class.isAssignableFrom(type.getJavaClass())) {
                return new ConstructorInvocation(type);
            }
        }

        Expression[] resolved = new Expression[children.length - 1];
        for (int i = 0; i < resolved.length; i++) {
            resolved[i] = children[i + 1].resolve(scope);
        }

        if (Instance.class.isAssignableFrom(resolved[0].getType().getJavaClass())) {
            Class[] paramTypes = new Class[resolved.length - 1];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = resolved[i + 1].getType().getJavaClassForSignature();
            }
            try {
                Method method = resolved[0].getType().getJavaClass().getMethod(name, paramTypes);
                return new MethodInvocation(method, resolved);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Method '" + name + "' with parameter types " + Arrays.toString(paramTypes) + " not found in class " + resolved[0].getType());
            }
        } else {
            Class[] paramTypes = new Class[resolved.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = resolved[i].getType().getJavaClassForSignature();
            }
            try {
                Method method = Builtins.class.getMethod(name, paramTypes);
                return new BuiltinInvocation(method, false, resolved);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Method '" + name + "' with parameter types " + Arrays.toString(Arrays.copyOfRange(paramTypes, 1, paramTypes.length)) + " not found in class " + resolved[0].getType());
            }
        }
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {
        boolean braces = parentPrecedence > Parser.PRECEDENCE_IMPLICIT;
        if (braces) {
            sb.append('(');
        }
        sb.append(children[0]);
        for (int i = 1; i < children.length; i++) {
            sb.append(' ');
            sb.append(children[i]);
        }
        if (braces) {
            sb.append(')');
        }
    }
}
