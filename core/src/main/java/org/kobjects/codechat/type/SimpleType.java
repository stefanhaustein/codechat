package org.kobjects.codechat.type;

public class SimpleType extends Type {
    private final String name;
    private final Class<?> javaClass;

    SimpleType(String name, Class<?> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        return javaClass == Object.class || ((other instanceof SimpleType) && ((SimpleType) other).javaClass == javaClass);
    }

    @Override
    public String getName() {
        return name;
    }
}
