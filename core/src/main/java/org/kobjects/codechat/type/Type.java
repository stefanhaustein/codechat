package org.kobjects.codechat.type;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.kobjects.codechat.lang.Tuple;

public abstract class Type {
    public static final Type NUMBER = new SimpleType("number", Double.class);
    public static final Type STRING = new SimpleType("string", String.class);
    public static final Type BOOLEAN = new SimpleType("boolean", Boolean.class);
    public static final Type VOID = new SimpleType("void", Void.TYPE);
    public static final Type ANY = new SimpleType("any", Object.class);

    public static Class<?> getJavaClassForSignature(Type type) {
        Class javaClass = type.getJavaClass();
        return javaClass == Double.class ? Double.TYPE : javaClass == Boolean.class ? Boolean.TYPE : javaClass;
    }

    public static Type of(Object o) {
        if (o == null) {
            return VOID;
        }
        if (o instanceof Type) {
            return new MetaType((Type) o);
        }
        if (o instanceof Tuple) {
            return ((Tuple) o).getType();
        }
        return forJavaType(o.getClass());
    }

    public static Type forJavaType(java.lang.reflect.Type javaType) {
        if (javaType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) javaType;
            Class rawType = (Class) parameterizedType.getRawType();
            if (List.class.isAssignableFrom(rawType)) {
                return new ArrayType(forJavaType(parameterizedType.getActualTypeArguments()[0]));
            }
            javaType = rawType;
        }

        if (javaType instanceof Class) {
            Class javaClass = (Class) javaType;
            if (Type.class.isAssignableFrom(javaClass)) {
                return new MetaType(Type.ANY);
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
            try {
                return (TupleType) javaClass.getField("TYPE").get(null);
            } catch (Exception e) {}
            return new SimpleType(javaClass.getSimpleName().toLowerCase(), javaClass);
        }
        throw new RuntimeException("Unrecognized Java type: " + javaType);
    }


    // FIXME: Abstract
    public boolean isAssignableFrom(Type other) {
        return getJavaClass().isAssignableFrom(other.getJavaClass());
    }

    public abstract String getName();

    // FIXME: Remove
    public abstract Class<?> getJavaClass();

    @Override
    public final String toString() {
        return getName();
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
