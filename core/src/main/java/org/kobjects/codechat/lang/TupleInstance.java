package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.type.TupleType;

public abstract class TupleInstance implements Tuple, Instance {
    private int id;
    private Environment environment;
    protected TupleInstance(Environment environment, int id) {
        this.environment = environment;
        this.id = id;
    }

    public String toString() {
        String name = environment.constants.get(this);
        return  name != null ? name : (getType() + "#" + id);
    }

    @Override
    public void serializeStub(AnnotatedStringBuilder asb) {
        asb.append("new ").append(getType() + "#" + id).append(";\n");
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
                asb.append("new ");
                if (environment.constants.containsKey(this)) {
                    asb.append(getType().getName());
                    break;
                }
            case STUB_SERIALIZED:
                asb.append(toString(), new InstanceLink(this));
                break;
            default:
                System.err.println("Redundant serialization of " + toString());
                asb.append(toString()).append("\n");
                return;
        }

        asb.append(" :: \n");
        serializationContext.setState(this, SerializationContext.SerializationState.STUB_SERIALIZED);
        for (TupleType.PropertyDescriptor propertyDescriptor: getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                MaterialProperty materialProperty = (MaterialProperty) property;
                if (materialProperty.modified()) {
                    Object value = property.get();
                    if (value != null) {
                        asb.append("  ");
                        asb.append(propertyDescriptor.name);
                        asb.append(" = ");
                        asb.append(Formatting.toLiteral(value));
                        asb.append(";\n");
                    }
                }
            }
        }
        asb.append("end;\n");
        serializationContext.setState(this, SerializationContext.SerializationState.FULLY_SERIALIZED);
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
