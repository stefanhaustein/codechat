package org.kobjects.codechat.type;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.HasDocumentationDetail;

public class EnumType extends AbstractType implements HasDocumentationDetail {
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

    @Override
    public void printDocumentationDetail(AnnotatedStringBuilder asb) {
        asb.append("Values: ");

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
