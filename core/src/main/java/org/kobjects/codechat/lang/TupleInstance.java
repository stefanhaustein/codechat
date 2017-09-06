package org.kobjects.codechat.lang;

import java.util.Collection;
import java.util.Map;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.type.TupleType;

public abstract class TupleInstance implements Tuple, Instance {
    private int id;
    protected TupleInstance(Environment environment, int id) {
        this.id = id;
    }

    public String toString() {
        return getType() + "#" + id;
    }


    @Override
    public void serialize(AnnotatedStringBuilder asb, Detail detail, Map<Dependency, Environment.SerializationState> serializationStateMap) {
        switch (detail) {
            case DECLARATION:
                asb.append(" new ").append(toString(), new InstanceLink(this)).append(";\n");
                break;
            case DEFINITION:
                serializeDefinition(asb);
                break;
            case DETAIL:
                serializeDetails(asb);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void serializeDefinition(AnnotatedStringBuilder asb) {
        asb.append(toString(), new InstanceLink(this));
        asb.append("{");
        boolean first = true;
        for (TupleType.PropertyDescriptor propertyDescriptor: getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                MaterialProperty materialProperty = (MaterialProperty) property;
                if (materialProperty.modified()) {
                    Object value = property.get();
                    if (value != null) {
                        if (first) {
                            first = false;
                        } else {
                            asb.append(", ");
                        }
                        asb.append(propertyDescriptor.name);
                        asb.append(": ");
                        asb.append(Formatting.toLiteral(value));
                    }
                }
            }
        }
        asb.append("};\n");
    }

    private void serializeDetails(AnnotatedStringBuilder asb) {
        for (TupleType.PropertyDescriptor propertyDescriptor : getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                MaterialProperty materialProperty = (MaterialProperty) property;
                if (materialProperty.modified()) {
                    Object value = property.get();
                    if (value != null) {
                        asb.append(toString());
                        asb.append(".");
                        asb.append(propertyDescriptor.name);
                        asb.append(" = ");
                        asb.append(Formatting.toLiteral(value));
                        asb.append(";\n");
                    }
                }
            }
        }
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        for (TupleType.PropertyDescriptor propertyDescriptor : getType ().properties()) {
            if (propertyDescriptor.writable) {
                Object deps = getProperty(propertyDescriptor.index);
                if (deps instanceof Dependency) {
                    result.add((Dependency) deps);
                } else if (deps instanceof HasDependencies) {
                    ((HasDependencies) deps).getDependencies(environment, result);
                }
            }
        }
    }

    public int getId() {
        return id;
    }
}
