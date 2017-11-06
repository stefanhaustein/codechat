package org.kobjects.codechat.type;

import java.util.TreeMap;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Property;

public abstract class InstanceType extends Type implements Documented {
    private final TreeMap<String, PropertyDescriptor> propertyMap = new TreeMap<>();
    private final boolean singleton;

    public InstanceType() {
        this(false);
    }

    public InstanceType(boolean singleton) {
        this.singleton = singleton;
    }

    public InstanceType addProperty(int index, String name, Type type, boolean writable, String documentation) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation));
        return this;
    }

    public PropertyDescriptor getProperty(String name) {
        PropertyDescriptor propertyDescriptor = propertyMap.get(name);
        if (propertyDescriptor == null) {
            throw new IllegalArgumentException("Propery '" + name + "' does not exist");
        }
        return propertyDescriptor;
    }

    public Iterable<PropertyDescriptor> properties() {
        return propertyMap.values();
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        return other instanceof InstanceType && other.getName().equals(getName());
    }

    @Override
    public AnnotatedCharSequence getDocumentation() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
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
        return asb.build();
    }

    public class PropertyDescriptor implements Documented {
        public final String name;
        public final Type type;
        public final int index;
        public final boolean writable;
        public final String documentation;

        private PropertyDescriptor(String name, Type type, int index, boolean writable, String documentation) {
            this.name = name;
            this.type = type;
            this.index = index;
            this.writable = writable;
            this.documentation = documentation;
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
        public AnnotatedCharSequence getDocumentation() {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();

            asb.append(singleton ? InstanceType.this.getName().toLowerCase() : InstanceType.this.getName(), new DocumentedLink(InstanceType.this));
            asb.append(".").append(name).append(": ");
            asb.append(type.toString(), type instanceof Documented ? new DocumentedLink((Documented) type) : null);
            asb.append("\n");
            asb.append(documentation);
            return asb.build();
        }
    }


}
