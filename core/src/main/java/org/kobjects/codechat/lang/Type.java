package org.kobjects.codechat.lang;

import java.lang.reflect.ParameterizedType;
import java.util.List;

public class Type {
    public static final Type NUMBER = new Type(Double.class);
    public static final Type STRING = new Type(String.class);
    public static final Type BOOLEAN = new Type(Boolean.class);
    public static final Type VOID = new Type(Void.TYPE);
    public static final Type META_TYPE = new Type(Type.class);

    private final Class javaClass;

    public static Type forJavaType(java.lang.reflect.Type javaType) {
        if (javaType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) javaType;
            Class rawType = (Class) parameterizedType.getRawType();
            if (List.class.isAssignableFrom(rawType)) {
                return new ListType(forJavaType(parameterizedType.getActualTypeArguments()[0]));
            }
            javaType = rawType;
        }

        if (javaType instanceof Class) {
            Class javaClass = (Class) javaType;
            if (javaClass == Type.class) {
                return META_TYPE;
            }
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

        throw new RuntimeException("Unrecognized Java type: " + javaType);
    }


    protected Type(Class javaClass) {
        this.javaClass = javaClass;
    }

    public boolean isAssignableFrom(Type other) {
        return javaClass.isAssignableFrom(other.javaClass);
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }


    public Class<?> getJavaClassForSignature() {
        return javaClass == Double.class ? Double.TYPE : javaClass == Boolean.class ? Boolean.TYPE : javaClass;
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
        return isAssignableFrom(t2) && t2.isAssignableFrom(this);
    }
}
