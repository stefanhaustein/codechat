package org.kobjects.codechat.lang;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.TreeMap;

public class JavaType extends Type {

    TreeMap<String, Property> propertyMap;

    protected JavaType(Class javaClass) {
        super(javaClass);
    }

    public TreeMap<String, Property> getPropertyMap() {
        if (propertyMap == null) {
            propertyMap = new TreeMap<>();
            for (Field field : javaClass.getFields()) {
                if (org.kobjects.codechat.lang.Property.class.isAssignableFrom(field.getType())) {

                    java.lang.reflect.Type javaType = field.getGenericType();
                    while (javaType instanceof Class) {
                        javaType = ((Class) javaType).getGenericSuperclass();
                    }
                    java.lang.reflect.Type propertyType = ((ParameterizedType) javaType).getActualTypeArguments()[0];
                    Type type = Type.forJavaType(propertyType);

                    propertyMap.put(field.getName(), new Property(field.getName(), type, field));
                }
            }
        }
        return propertyMap;
    }

    public Iterable<Property> properties() {
        return getPropertyMap().values();
    }

    public Property getProperty(String name) {
        Property result = getPropertyMap().get(name);
        if (result == null) {
            throw new RuntimeException("Property '" + name + "' does not exist in " + this);
        }
        return result;
    }

    public class Property {
        String name;
        public Type type;
        Field field;

        Property(String name, Type type, Field field) {
            this.name = name;
            this.type = type;
            this.field = field;
        }

        public boolean isMutable() {
            return Settable.class.isAssignableFrom(field.getType());
        }

        public Object get(Object instance) {
            try {
                return ((org.kobjects.codechat.lang.Property) field.get(instance)).get();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public void set(Object instance, Object value) {
            try {
                ((org.kobjects.codechat.lang.Settable) field.get(instance)).set(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public org.kobjects.codechat.lang.Property getProperty(Object instance) {
            try {
                return (org.kobjects.codechat.lang.Property) field.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
