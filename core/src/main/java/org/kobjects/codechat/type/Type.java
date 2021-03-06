package org.kobjects.codechat.type;

public interface Type extends Typed {
    Type NUMBER = new SimpleType("Number", Double.class);
    Type STRING = new SimpleType("String", String.class );
    Type BOOLEAN = new SimpleType("Boolean", Boolean.class);
    //public static final Type VOID = new SimpleType("Void", Void.TYPE, "Used as a replacement for the return type for functions that don't return a value.");
    Type ANY = new SimpleType("Any", Object.class);

    boolean isAssignableFrom(Type other);

    String getName();
}
