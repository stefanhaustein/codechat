package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import java.util.List;
import org.kobjects.codechat.Environment;
import org.kobjects.codechat.Processor;

public class Implicit extends Node {
    public Node[] children;
    public Implicit(Node... children) {
        this.children = children;
    }


    @Override
    public Object eval(Environment environment) {
        if (!(children[0] instanceof Identifier)) {
            throw new RuntimeException("verb expected as first parameter");
        }
        String name = ((Identifier) children[0]).name;
        Object o = children[1].eval(environment);
        if (o instanceof Class) {
            Class c = (Class) o;
            switch (name) {
                case "create":
                    return environment.instantiate(c);
                default:
                    throw new RuntimeException(c.getSimpleName().toLowerCase() + " can't " + name);
            }
        }

        if (o instanceof String) {
            switch (name) {
                case "save":
                    environment.save((String) o);
                    return null;
                case "load":
                    environment.load((String) o);
                    return null;
            }
        }

        Object[] param = new Object[children.length - 2];
        Class[] pc = new Class[param.length];
        for (int i = 0; i < param.length; i++) {
           Object pi = param[i] = children[i + 2].eval(environment);
           Class pci = pi.getClass();
           pc[i] = pci == Double.class ? Double.TYPE : pci;
        }
        Class c = o.getClass();
        try {
            Method method = c.getMethod(name, pc);
            return method.invoke(o, param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {
        boolean braces = parentPrecedence > Processor.PRECEDENCE_IMPLICIT;
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
