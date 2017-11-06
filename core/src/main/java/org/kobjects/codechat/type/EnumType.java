package org.kobjects.codechat.type;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.EnumLiteral;

public class EnumType extends Type implements Documented {
    private final String name;
    private final EnumLiteral[] values;

    public EnumType(String name, EnumLiteral... values) {
        this.name = name;
        this.values = values;
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
            if (literal.name().equals(name)) {
                return literal;
            }
        }
        throw new RuntimeException("Literal '" + name + "' not found in " + this.name);
    }

    @Override
    public AnnotatedCharSequence getDocumentation() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append(name);
        asb.append(": enumeration type of the values ");

        for (int i = 0; i < values.length; i++) {
            if (i == values.length - 1) {
                asb.append(" and ");
            } else if (i > 0) {
                asb.append(", ");
            }
            asb.append(values[i].name());
        }
        return asb.build();
    }
}
