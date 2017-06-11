package org.kobjects.codechat.lang;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Instance {
    protected int id;
    protected Instance(Environment environment, int id) {
        this.id = id;
    }


    public String toString() {
        String name = getClass().getSimpleName();
        if (name.endsWith("Statement")) {
            name = name.substring(0, name.length() - "Statement".length());
        }
        return name.toLowerCase() + "#" + id;
    }

    public void dump(Writer writer, boolean ctor) throws IOException {
        if (ctor) {
            writer.write(toString());
            writer.write("(");
        }
        boolean first = true;

        for (Field field : getClass().getFields()) {
            if (MaterialProperty.class.isAssignableFrom(field.getType())) {
                try {
                    MaterialProperty property = (MaterialProperty) field.get(this);
                    if (property.modified()) {
                        Object value = property.get();
                        if (value != null) {
                            if (value instanceof String || value instanceof Boolean || value instanceof Number) {
                                if (ctor) { //  && !field.getName().equals("rotationSpeed")) {
                                    if (first) {
                                        first = false;
                                    } else {
                                        writer.write(", ");
                                    }
                                    writer.write(field.getName());
                                    writer.write(": ");
                                    writer.write(Formatting.toLiteral(value));
                                }
                            } else {
                                if (!ctor) {
                                    writer.write(toString());
                                    writer.write('.');
                                    writer.write(field.getName());
                                    writer.write(" = ");
                                    writer.write(Formatting.toLiteral(value));
                                    writer.write(";\n");
                                }
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ctor) {
            writer.write(")\n");
        }

        /*
        for (Method method: getClass().getMethods()) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0 && !method.getName().equals("getClass")) {
                try {
                    Object value = method.invoke(this);
                    if (value != null && !"".equals(value) && !value.equals(0.0) && !value.equals(0) && !value.equals(Boolean.FALSE)) {
                        writer.write(value.toString());
                        writer.write('\n');
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        */

    }
}
