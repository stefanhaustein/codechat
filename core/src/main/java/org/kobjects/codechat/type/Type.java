package org.kobjects.codechat.type;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;

public class Type {
    public static final Type NUMBER = new Type(Double.class);
    public static final Type STRING = new Type(String.class);
    public static final Type BOOLEAN = new Type(Boolean.class);
    public static final Type VOID = new Type(Void.TYPE);
    public static final Type META_TYPE = new Type(Type.class);

    private static final HashMap<Class, Type> cache = new HashMap<>();

    final Class javaClass;

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
            Type result = cache.get(javaClass);
            if (result != null) {
                return result;
            }
            if (Type.class.isAssignableFrom(javaClass)) {
                result = META_TYPE;
            } else if (javaClass == Boolean.class || javaClass == Boolean.TYPE) {
                result = BOOLEAN;
            } else if (javaClass == Double.class || javaClass == Double.TYPE) {
                result = NUMBER;
            } else if (javaClass == Void.class || javaClass == Void.TYPE) {
                result = VOID;
            } else {
                result = new JavaType(javaClass);
            }
            cache.put(javaClass, result);
            return result;
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
