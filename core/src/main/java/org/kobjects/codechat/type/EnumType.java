package org.kobjects.codechat.type;

import java.util.List;
import org.kobjects.codechat.lang.Annotation;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.EnumLiteral;

public class EnumType extends Type implements Documented {
    private final String name;
    private final EnumLiteral[] values;

    public EnumType(String name, String... values) {
        this.name = name;
        this.values = new EnumLiteral[values.length];
        for (int i = 0; i < values.length; i++) {
            this.values[i] = new EnumLiteral(this, values[i]);
        }
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        return other == this;
    }

    @Override
    public String getName() {
        return name;
    }

    EnumLiteral[] getValues() {
        return values;
    }

    public EnumLiteral getValue(String name) {
        for (EnumLiteral literal: values) {
            if (literal.getName().equals(name)) {
                return literal;
            }
        }
        throw new RuntimeException("Literal '" + name + "' not found in " + this.name);
    }

    @Override
    public String getDocumentation(List<Annotation> links) {
        StringBuilder sb = new StringBuilder(name);
        sb.append(": enumeration type of the values ");

        for (int i = 0; i < values.length; i++) {
            if (i == values.length - 1) {
                sb.append(" and ");
            } else if (i > 0) {
                sb.append(", ");
            }
            sb.append(values[i].getName());
        }
        return sb.toString();
    }
}
