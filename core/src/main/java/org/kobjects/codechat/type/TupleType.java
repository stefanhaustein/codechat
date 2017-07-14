package org.kobjects.codechat.type;

import java.util.List;
import java.util.TreeMap;
import org.kobjects.codechat.lang.Annotation;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Settable;
import org.kobjects.codechat.lang.Tuple;

public class TupleType extends Type implements Documented {
    private final TreeMap<String, PropertyDescriptor> propertyMap = new TreeMap<>();
    private final String name;
    private final String documentation;

    public TupleType(String name, String documentation) {
        this.name = name;
        this.documentation = documentation;
    }

    public void addProperty(int index, String name, Type type, boolean writable, String documentation) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation));
    }

    public PropertyDescriptor getProperty(String name) {
        if (name.equals("rotationSpeed")) {
            name = "rotation";
        }
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
    public String getDocumentation(List<Annotation> links) {
        StringBuilder sb = new StringBuilder(documentation);
        sb.append("\n\nProperties:\n\n");
        for (PropertyDescriptor propertyDescriptor: properties()) {
            sb.append(propertyDescriptor.name).append(": ").append(propertyDescriptor.type).append("\n");
        }
        return sb.toString();
    }

    public class PropertyDescriptor {
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
            ((Settable) tuple.getProperty(index)).set(value);
        }

        public Object get(Tuple tuple) {
            return tuple.getProperty(index).get();
        }

    }


}
