package org.kobjects.codechat.type;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EnumLiteral;

public class EnumType extends AbstractType {
    private final EnumLiteral[] values;
    private final String name;

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
        throw new RuntimeException("Literal '" + name + "' not in {" + this.values+ "}");
    }

    public void printDocumentation(AnnotatedStringBuilder asb) {
        asb.append("Enumeration type of the values ");

        for (int i = 0; i < values.length; i++) {
            if (i == values.length - 1) {
                asb.append(" and ");
            } else if (i > 0) {
                asb.append(", ");
            }
            asb.append(values[i].name());
        }
    }
}
