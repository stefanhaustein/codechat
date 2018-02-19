package org.kobjects.codechat.type;


import jdk.nashorn.internal.ir.annotations.Ignore;

public class SimpleType extends AbstractType {
    private final Class<?> javaClass;
    private final String name;

    SimpleType(String name, Class<?> javaClass) {
        this.name = name;
        this.javaClass = javaClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        return javaClass == Object.class || ((other instanceof SimpleType) && ((SimpleType) other).javaClass == javaClass);
    }

}
