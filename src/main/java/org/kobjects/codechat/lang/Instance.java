package org.kobjects.codechat.lang;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;

public class Instance {
    protected int id;
    protected Environment environment;
    protected Instance(Environment environment, int id) {
        this.environment = environment;
        this.id = id;
    }


    public String toString() {
        return getClass().getSimpleName().toLowerCase() + "#" + id;
    }

    public void dump(Writer writer) throws IOException {
        for (Method method: getClass().getMethods()) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0 && !method.getName().equals("getClass")) {
                try {
                    Object value = method.invoke(this);
                    if (value != null && !"".equals(value) && !value.equals(0.0) && !value.equals(0) && !value.equals(Boolean.FALSE)) {
                        writer.write(toString());
                        writer.write('.');
                        writer.write(method.getName().substring(3).toLowerCase());
                        writer.write(" = ");
                        writer.write(value.toString());
                        writer.write('\n');
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
