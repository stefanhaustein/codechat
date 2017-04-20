package org.kobjects.codechat.lang;

public class Type {
    public static final Type NUMBER = new Type(Double.class);
    public static final Type STRING = new Type(String.class);
    public static final Type BOOLEAN = new Type(Boolean.class);

    private final Class javaClass;

    public static Type forJavaClass(Class<?> javaClass) {
        if (javaClass == Double.class) {
            return NUMBER;
        }
        return new Type(javaClass);
    }


    private Type (Class javaClass) {
        this.javaClass = javaClass;
    }

    public boolean isAssignableFrom(Type other) {
        return javaClass.isAssignableFrom(other.javaClass);
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }


    public Class<?> getJavaClassForSignature() {
        return javaClass == Double.class ? Double.TYPE : javaClass;
    }

    public String toString() {
        if (javaClass == Double.class) {
            return "number";
        }
        return javaClass.getSimpleName().toLowerCase();
    }
}
