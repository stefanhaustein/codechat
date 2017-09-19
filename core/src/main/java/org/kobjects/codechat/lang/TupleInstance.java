package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
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
    public void serializeStub(AnnotatedStringBuilder asb) {
        asb.append("new ").append(toString()).append(";\n");
    }

    void serializeDependencies(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        DependencyCollector dependencyCollector = new DependencyCollector();
        getDependencies(serializationContext.getEnvironment(), dependencyCollector);

        for (Entity entity: dependencyCollector.getStrong()) {
            if (serializationContext.getState(entity) == SerializationContext.SerializationState.UNVISITED) {
                entity.serializeStub(asb);
                serializationContext.setState(entity, SerializationContext.SerializationState.STUB_SERIALIZED);
            }
        }
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializeDependencies(asb, serializationContext);
        switch (serializationContext.getState(this)) {
            case UNVISITED:
                serializeDefinition(asb);
                break;
            case STUB_SERIALIZED:
                serializeDetails(asb);
                break;
            default:
                System.err.println("Redundant serialization of " + toString());
                asb.append(toString()).append("\n");
                break;
        }
        serializationContext.setState(this, SerializationContext.SerializationState.FULLY_SERIALIZED);
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
    public void getDependencies(Environment environment, DependencyCollector result) {
        for (TupleType.PropertyDescriptor propertyDescriptor : getType ().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                Object value = property.get();
                if (value instanceof Entity) {
                    result.addStrong((Entity) value);
                } else if (value instanceof HasDependencies) {
                    ((HasDependencies) value).getDependencies(environment, result);
                }
            }
            for (Object listener : property.getListeners()) {
                if (listener instanceof OnInstance) {
                    result.addWeak((OnInstance) listener);
                }
            }
        }
    }

    public int getId() {
        return id;
    }
}
