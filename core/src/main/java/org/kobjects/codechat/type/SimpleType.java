package org.kobjects.codechat.type;

public class SimpleType extends Type {
    private final String name;
    private final Class<?> javaClass;

    SimpleType(String name, Class<?> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    @Override
    public Class<?> getJavaClass() {
        return javaClass;
    }

    @Override
    public String getName() {
        return name;
    }
}
