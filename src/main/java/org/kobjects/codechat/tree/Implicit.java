package org.kobjects.codechat.tree;

import java.lang.reflect.Method;
import java.util.List;
import org.kobjects.codechat.Environment;

public class Implicit extends Node {

    public Implicit(Node... children) {
        super(children);
    }

    public Implicit(List<Node> children) {
        super(children);
    }

    @Override
    public Object eval(Environment environment) {
        if (children[0] instanceof Identifier) {
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
        throw new RuntimeException("verb expected as first parameter");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(children[0]);
        for (int i = 1; i < children.length; i++) {
            sb.append(' ');
            sb.append(children[i]);
        }
        return sb.toString();
    }

}
