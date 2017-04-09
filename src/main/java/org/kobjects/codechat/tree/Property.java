package org.kobjects.codechat.tree;

import java.lang.reflect.Method;
import org.kobjects.codechat.Environment;

public class Property extends Node {
    String name;
    Node base;

    public Property(Node node, String name) {
        this.base = node;
        this.name = name;
    }

    @Override
    public Object eval(Environment environment) {
        Object value = base.eval(environment);
        try {
            Method method = value.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            return method.invoke(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assign(Environment environment, Object value) {
        Object baseValue = base.eval(environment);
        try {
            Class c = value.getClass();
            Method method = baseValue.getClass().getMethod("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1),
                    c == Double.class ? Double.TYPE : c);
            method.invoke(baseValue, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return base + "." + name;
    }
}
