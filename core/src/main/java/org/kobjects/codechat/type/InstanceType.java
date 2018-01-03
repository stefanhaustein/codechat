package org.kobjects.codechat.type;

import java.util.Collection;
import java.util.TreeMap;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.Property;

public abstract class InstanceType<T extends Instance> extends Type implements Documented {
    private final TreeMap<String, PropertyDescriptor> propertyMap = new TreeMap<>();
    private final boolean singleton;

    public InstanceType() {
        this(false);
    }

    public InstanceType(boolean singleton) {
        this.singleton = singleton;
    }

    public InstanceType addProperty(int index, String name, Type type, boolean writable, String documentation, Expression initializer) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation, initializer));
        return this;
    }

    public InstanceType addProperty(int index, String name, Type type, boolean writable, String documentation) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation, null));
        return this;
    }

    public PropertyDescriptor getProperty(String name) {
        PropertyDescriptor propertyDescriptor = propertyMap.get(name);
        if (propertyDescriptor == null) {
            throw new IllegalArgumentException("Propery '" + name + "' does not exist");
        }
        return propertyDescriptor;
    }

    public Collection<PropertyDescriptor> properties() {
        return propertyMap.values();
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        return other instanceof InstanceType && other.getName().equals(getName());
    }

    @Override
    public void printDocumentation(AnnotatedStringBuilder asb) {
        asb.append("\nProperties: ");
        boolean first = true;
        for (PropertyDescriptor propertyDescriptor: properties()) {
            if (first) {
                first = false;
            } else {
                asb.append(", ");
            }
            asb.append(propertyDescriptor.name, new DocumentedLink(propertyDescriptor));
        }
    }

    public class PropertyDescriptor implements Documented, Printable {
        public final String name;
        public final Type type;
        public final int index;
        public final boolean writable;
        public final String documentation;
        public final Expression initializer;

        private PropertyDescriptor(String name, Type type, int index, boolean writable, String documentation, Expression initializer) {
            this.name = name;
            this.type = type;
            this.index = index;
            this.writable = writable;
            this.documentation = documentation;
            this.initializer = initializer;
        }

        public Property getProperty(Instance tuple) {
            return tuple.getProperty(index);
        }

        public void set(Instance tuple, Object value) {
            tuple.getProperty(index).set(value);
        }

        public Object get(Instance tuple) {
            return tuple.getProperty(index).get();
        }

        @Override
        public void printDocumentation(AnnotatedStringBuilder asb) {
            asb.append(singleton ? InstanceType.this.getName().toLowerCase() : InstanceType.this.getName(), new DocumentedLink(InstanceType.this));
            asb.append(".").append(name).append(": ");
            asb.append(type.toString(), type instanceof Documented ? new DocumentedLink((Documented) type) : null);
            asb.append("\n");
            asb.append(documentation);
        }

        @Override
        public void print(AnnotatedStringBuilder asb, Flavor flavor) {
            asb.append("  ");
            asb.append(name);
            asb.append(" = ");
            initializer.toString(asb, 6);
            asb.append('\n');
        }
    }


    public boolean isInstantiable() {
        return !singleton;
    }

    public T createInstance(Environment environment) {
        throw new UnsupportedOperationException();
    }
}
