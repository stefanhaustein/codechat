package org.kobjects.codechat.type;

public abstract class Type implements Typed {
    public static final Type NUMBER = new SimpleType("Number", Double.class, "Number: A 64 bit IEEE floating point number.");
    public static final Type STRING = new SimpleType("String", String.class, "String: A string of characters.");
    public static final Type BOOLEAN = new SimpleType("Boolean", Boolean.class, "Boolean: true or false.");
    //public static final Type VOID = new SimpleType("Void", Void.TYPE, "Used as a replacement for the return type for functions that don't return a value.");
    public static final Type ANY = new SimpleType("Any", Object.class, "Any type.h");

    public static Type of(Object o) {
        if (o instanceof Type) {
            return new MetaType((Type) o);
        }
        if (o instanceof Typed) {
            Type result = ((Typed) o).getType();
            if (result == null) {
                throw  new RuntimeException("Typed.getType null for" + o + " class " + o.getClass());
            }
            return result;
        }
        if (o instanceof Boolean) {
            return Type.BOOLEAN;
        }
        if (o instanceof Double) {
            return Type.NUMBER;
        }
        if (o instanceof String) {
            return Type.STRING;
        }
        if (o instanceof Type) {
            return new MetaType((Type) o);
        }
        return Type.ANY;
    }


    public abstract boolean isAssignableFrom(Type other);

    public abstract String getName();

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

    @Override
    public Type getType() {
        return new MetaType(this);
    }

}
