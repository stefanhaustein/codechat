package org.kobjects.codechat.instance;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.HasDependencies;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.Classifier;

public abstract class AbstractInstance implements Instance {
    protected final Environment environment;

    protected AbstractInstance(Environment environment) {
        this.environment = environment;
    }

    public String toString() {
        return environment.getName(this);
    }

    @Override
    public void print(AnnotatedStringBuilder asb, Flavor flavor) {
        String constantName = environment.getConstantName(this);

        if (flavor == Printable.Flavor.SAVE) {
            if (constantName != null) {
                asb.append("let ");
                asb.append(constantName);
                asb.append(" := ");
            }
            asb.append("new ");
        }

        if (constantName != null) {
            if (flavor == Printable.Flavor.SAVE) {
                asb.append(getType().toString(), new DocumentedLink(getType()));
            } else {
                asb.append(constantName);
            }
        } else {
            asb.append(getType() + "#" + environment.getId(this), new InstanceLink(this));
        }

        if (flavor == Printable.Flavor.LIST) {
            asb.append("\n");
        } else {
            asb.append(" :: \n");
            for (Classifier.PropertyDescriptor propertyDescriptor : getType().properties()) {
                Property property = getProperty(propertyDescriptor.index);
                if (property instanceof MaterialProperty) {
                    MaterialProperty materialProperty = (MaterialProperty) property;
                    if (materialProperty.modified()) {
                        Object value = property.get();
                        if (value != null) {
                            if (flavor == Printable.Flavor.EDIT ||
                                    (value instanceof Instance) == (flavor == Printable.Flavor.SAVE2)) {
                                asb.append("  ");
                                asb.append(propertyDescriptor.name);
                                asb.append(" := ");
                                asb.append(Formatting.toLiteral(value));
                                asb.append(";\n");
                            }
                        }
                    }
                }
            }
            asb.append("end\n");
        }
    }


    @Override
    public void getDependencies(DependencyCollector result) {
        for (Classifier.PropertyDescriptor propertyDescriptor : getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                Object value = property.get();
                if (value instanceof Instance) {
                    result.add((Instance) value);
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

    @Override
    public boolean needsTwoPhaseSerilaization() {
        for (Classifier.PropertyDescriptor propertyDescriptor : getType().properties()) {
            Property property = getProperty(propertyDescriptor.index);
            if (property instanceof MaterialProperty) {
                MaterialProperty materialProperty = (MaterialProperty) property;
                if (materialProperty.modified()) {
                    if (property.get() instanceof Instance) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public abstract Classifier<?> getType();


    @Override
    public Environment getEnvironment() {
        return environment;
    }
}
