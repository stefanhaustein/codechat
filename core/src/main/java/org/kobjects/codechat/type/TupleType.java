package org.kobjects.codechat.type;

import java.util.TreeMap;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Settable;
import org.kobjects.codechat.lang.Tuple;

public class TupleType extends Type {
    private final TreeMap<String, PropertyDescriptor> propertyMap = new TreeMap<>();
    private final Class<? extends Tuple> javaClass;
    private final String name;

    public TupleType(String name, Class<? extends Tuple> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    public void addProperty(int index, String name, Type type, boolean writable) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable));
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

    @Override
    public boolean isAssignableFrom(Type other) {
        return other instanceof TupleType && ((TupleType) other).name.equals(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getJavaClass() {
        return javaClass;
    }

    public class PropertyDescriptor {
        public final String name;
        public Type type;
        public int index;
        public boolean writable;

        private PropertyDescriptor(String name, Type type, int index, boolean writable) {
            this.name = name;
            this.type = type;
            this.index = index;
            this.writable = writable;
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
