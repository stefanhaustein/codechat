package org.kobjects.codechat.expr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.api.Builtins;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Parser;

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

        // BAD HACK!!!
        if ("delete".equals(name) && children[1] instanceof Identifier) {
            environment.variables.remove(((Identifier) children[1]).name);
        }

        if (o instanceof Instance) {
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
        } else {
            Object[] param = new Object[children.length - 1];
            Class[] pc = new Class[param.length];
            for (int i = 0; i < param.length; i++) {
                Object pi = param[i] = i == 0 ? o : children[i + 2].eval(environment);
                Class pci = pi.getClass();
                pc[i] = pci == Double.class ? Double.TYPE : pci;
            }
            try {
                Method method = Builtins.class.getMethod(name, pc);
                return method.invoke(environment.builtins, param);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }  catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
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
