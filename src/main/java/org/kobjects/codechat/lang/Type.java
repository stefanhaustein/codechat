package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.Expression;

public class Type {
    public static final Type NUMBER = new Type(Double.class);
    public static final Type STRING = new Type(String.class);
    public static final Type BOOLEAN = new Type(Boolean.class);
    public static final Type VOID = new Type(Void.TYPE);

    private final Class javaClass;

    public static Type forJavaClass(Class<?> javaClass) {
        if (javaClass == Boolean.class || javaClass == Boolean.TYPE) {
            return BOOLEAN;
        }
        if (javaClass == Double.class || javaClass == Double.TYPE) {
            return NUMBER;
        }
        if (javaClass == Void.class || javaClass == Void.TYPE) {
            return VOID;
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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Type)) {
            return false;
        }
        Type t2 = (Type) other;
        return javaClass.equals(t2.getJavaClass());
    }
}
