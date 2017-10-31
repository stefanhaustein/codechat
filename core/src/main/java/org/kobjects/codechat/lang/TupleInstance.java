package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.type.TupleType;

public abstract class TupleInstance implements Tuple, Instance {

    public static void getDependencies(Tuple tuple, DependencyCollector result) {
        for (TupleType.PropertyDescriptor propertyDescriptor : tuple.getType ().properties()) {
            Property property = tuple.getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                Object value = property.get();
                if (value instanceof Entity) {
                    result.addStrong((Entity) value);
                } else if (value instanceof HasDependencies) {
                    ((HasDependencies) value).getDependencies(result);
                }
            }
            for (Object listener : property.getListeners()) {
                if (listener instanceof OnInstance) {
                    result.addWeak((OnInstance) listener);
                }
            }
        }
    }

    private int id;
    private Environment environment;
    private String unparsed;

    protected TupleInstance(Environment environment, int id) {
        this.environment = environment;
        this.id = id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public String toString() {
        String name = environment.constants.get(this);
        return  name != null ? name : (getType() + "#" + id);
    }

    private void appendConstructor(AnnotatedStringBuilder asb) {
        asb.append("new ");
        if (environment.constants.containsKey(this)) {
            asb.append(getType().toString(), new DocumentedLink(getType()));
        } else {
            asb.append(getType() + "#" + id, new EntityLink(this));
        }
    }

    @Override
    public void serializeStub(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        appendConstructor(asb);
        asb.append(";\n");
        serializationContext.setState(this, SerializationContext.SerializationState.STUB_SERIALIZED);
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializationContext.serializeDependencies(asb, this);

        switch (serializationContext.getState(this)) {
            case UNVISITED:
                appendConstructor(asb);
                break;
            case STUB_SERIALIZED:
                asb.append(toString(), new EntityLink(this));
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
    public void getDependencies(DependencyCollector result) {
        getDependencies(this, result);
    }

    public int getId() {
        return id;
    }


    @Override
    public void setUnparsed(String unparsed) {
        this.unparsed = unparsed;
    }

    @Override
    public String getUnparsed() {
        return unparsed;
    }

}
