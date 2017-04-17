package org.kobjects.codechat.expr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.api.Builtins;
import org.kobjects.codechat.lang.Environment;

public class Identifier extends Node {
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }
    @Override
    public Object eval(Environment environment) {
        try {
            Method method = Builtins.class.getMethod(name.equals("continue") ? "unpause" : name);
            try {
                return method.invoke(environment.builtins);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        } catch (NoSuchMethodException e) {
            Object result = environment.variables.get(name);
            if (result == null) {
                throw new RuntimeException("Undefined identifier: " + name);
            }
            return result;
        }

    }

    public void assign(Environment environment, Object value) {
        environment.variables.put(name, value);
    }

    public void toString(StringBuilder sb, int parentPrecedence) {
        sb.append(name);
    }
}
