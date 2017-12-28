package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Typed;

public abstract class Instance implements HasDependencies, Typed, Entity {

    public static final int NO_ID = 0;

    private final Environment environment;
    private int id;

    protected Instance(Environment environment, int id) {
        this.environment = environment;
        this.id = id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public String toString() {
        String name = environment.constants.get(this);
        return  name != null ? name : (getType() + "#" + getId());
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializationContext.setSerialized(this);

        if (serializationContext.getMode() == SerializationContext.Mode.SAVE) {
            asb.append("new ");
        }
        if (environment.constants.containsKey(this)) {
            if (serializationContext.getMode() == SerializationContext.Mode.SAVE) {
                asb.append(getType().toString(), new DocumentedLink(getType()));
            } else {
                asb.append(environment.constants.get(this));
            }
        } else {
            asb.append(getType() + "#" + getId(), new EntityLink(this));
        }

        if (serializationContext.getMode() == SerializationContext.Mode.LIST) {
            asb.append("\n");
        } else {
            asb.append(" :: \n");
            for (InstanceType.PropertyDescriptor propertyDescriptor : getType().properties()) {
                Property property = getProperty(propertyDescriptor.index);
                if (property instanceof MaterialProperty) {
                    MaterialProperty materialProperty = (MaterialProperty) property;
                    if (materialProperty.modified()) {
                        Object value = property.get();
                        if (value != null) {
                            if (serializationContext.getMode() == SerializationContext.Mode.EDIT ||
                                    (value instanceof Instance) == (serializationContext.getMode() == SerializationContext.Mode.SAVE2)) {
                                asb.append("  ");
                                asb.append(propertyDescriptor.name);
                                asb.append(" = ");
                                asb.append(Formatting.toLiteral(value));
                                asb.append(";\n");
                            } else if (serializationContext.getMode() == SerializationContext.Mode.SAVE) {
                                serializationContext.setNeedsPhase2(this);
                            }
                        }
                    }
                }
            }
            asb.append("end\n");
        }
    }

    public abstract Property getProperty(int index);

    @Override
    public abstract InstanceType<?> getType();

    @Override
    public void getDependencies(DependencyCollector result) {
        for (InstanceType.PropertyDescriptor propertyDescriptor : getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                Object value = property.get();
                if (value instanceof Entity) {
                    result.add((Entity) value);
                } else if (value instanceof HasDependencies) {
                    ((HasDependencies) value).getDependencies(result);
                }
            }
            for (Object listener : property.getListeners()) {
                if (listener instanceof OnInstance) {
                    result.add((OnInstance) listener);
                }
            }
        }
    }

    public synchronized int getId() {
        if (id < 0) {
            id = environment.createId(this);
        }
        return id;
    }


    public void delete() {
        String name = environment.constants.get(this);
        if (name != null) {
            RootVariable variable = environment.getRootVariable(name);
            environment.constants.remove(name);
            if (variable != null) {
                variable.delete();
            }
        }
    }
}
