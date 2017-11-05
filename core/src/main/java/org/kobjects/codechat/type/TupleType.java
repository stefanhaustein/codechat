package org.kobjects.codechat.type;

import java.util.List;
import java.util.TreeMap;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Tuple;

public class TupleType extends Type implements Documented {
    private final TreeMap<String, PropertyDescriptor> propertyMap = new TreeMap<>();
    private final String name;
    private final String documentation;
    private final boolean singleton;

    public TupleType(String name, String documentation) {
        this(name, documentation, false);
    }

    public TupleType(String name, String documentation, boolean singleton) {
        this.name = name;
        this.documentation = documentation;
        this.singleton = singleton;
    }

    public TupleType addProperty(int index, String name, Type type, boolean writable, String documentation) {
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
        return other instanceof TupleType && ((TupleType) other).name.equals(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AnnotatedCharSequence getDocumentation() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append(documentation);
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

        public Property getProperty(Tuple tuple) {
            return tuple.getProperty(index);
        }

        public void set(Tuple tuple, Object value) {
            tuple.getProperty(index).set(value);
        }

        public Object get(Tuple tuple) {
            return tuple.getProperty(index).get();
        }

        @Override
        public AnnotatedCharSequence getDocumentation() {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();

            asb.append(singleton ? TupleType.this.name.toLowerCase() : TupleType.this.name, new DocumentedLink(TupleType.this));
            asb.append(".").append(name).append(": ");
            asb.append(type.toString(), type instanceof Documented ? new DocumentedLink((Documented) type) : null);
            asb.append("\n");
            asb.append(documentation);
            return asb.build();
        }
    }


}
