package org.kobjects.codechat.tree;

import java.lang.reflect.Method;
import org.kobjects.codechat.Environment;

public class Property extends Node {
    String name;

    public Property(Node node, String name) {
        super(node);
        this.name = name;
    }

    @Override
    public Object eval(Environment environment) {
        Object base = children[0].eval(environment);
        try {
            Method method = base.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            return method.invoke(base);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assign(Environment environment, Object value) {
        Object base = children[0].eval(environment);
        try {
            Class c = value.getClass();
            Method method = base.getClass().getMethod("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1),
                    c == Double.class ? Double.TYPE : c);
            method.invoke(base, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return children[0] + "." + name;
    }
}
