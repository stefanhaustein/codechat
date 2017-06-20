package org.kobjects.codechat.lang;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;
import org.kobjects.codechat.type.SimpleType;
import org.kobjects.codechat.type.TupleType;

public abstract class Instance implements Tuple {
    protected int id;
    protected Instance(Environment environment, int id) {
        this.id = id;
    }


    public String toString() {
        return getType() + "#" + id;
    }

    public void serializeDeclaration(StringBuilder sb, List<Annotation> annotations) {
        Annotation.append(sb, toString(), this, annotations);
        sb.append("(");
        boolean first = true;
        for (TupleType.PropertyDescriptor propertyDescriptor: getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty && propertyDescriptor.type instanceof SimpleType) {
                MaterialProperty materialProperty = (MaterialProperty) property;
                if (materialProperty.modified()) {
                    Object value = property.get();
                    if (value != null) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(propertyDescriptor.name);
                        sb.append(": ");
                        sb.append(Formatting.toLiteral(value));
                    }
                }
            }
        }
        sb.append(");\n");
    }

    public void serializeDefinition(StringBuilder sb, boolean all) {
        for (TupleType.PropertyDescriptor propertyDescriptor : getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty && (all || !(propertyDescriptor.type instanceof SimpleType))) {
                MaterialProperty materialProperty = (MaterialProperty) property;
                if (materialProperty.modified()) {
                    Object value = property.get();
                    if (value != null) {
                        sb.append(toString());
                        sb.append('.');
                        sb.append(propertyDescriptor.name);
                        sb.append(" = ");
                        sb.append(Formatting.toLiteral(value));
                        sb.append(";\n");
                    }
                }
            }
        }
    }
}
