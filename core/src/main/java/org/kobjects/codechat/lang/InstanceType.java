package org.kobjects.codechat.lang;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.TreeMap;

public class InstanceType extends Type {

    TreeMap<String, Property> properties = new TreeMap<>();

    protected InstanceType(Class javaClass) {
        super(javaClass);

        for (Field field : javaClass.getFields()) {
            if (Property.class.isAssignableFrom(field.getType())) {

                java.lang.reflect.Type javaType = field.getGenericType();
                while (javaType instanceof Class) {
                    javaType = ((Class) javaType).getGenericSuperclass();
                }
                java.lang.reflect.Type propertyType = ((ParameterizedType) javaType).getActualTypeArguments()[0];
                Type type = Type.forJavaType(propertyType);

                properties.put(field.getName(), new Property(field.getName(), type, field));
            }
        }

    }

    public Iterable<Property> properties() {
        return properties.values();
    }

    public Property getProperty(String name) {
        Property result = properties.get(name);
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

        public void set(Instance instance, Object eval) {
            try {
                ((Property) field.get(instance)).set(instance, eval);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
