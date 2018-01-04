package org.kobjects.codechat.type;

public interface Type extends Typed {
    Type NUMBER = new SimpleType("Number", Double.class, "Number: A 64 bit IEEE floating point number.");
    Type STRING = new SimpleType("String", String.class, "String: A string of characters.");
    Type BOOLEAN = new SimpleType("Boolean", Boolean.class, "Boolean: true or false.");
    //public static final Type VOID = new SimpleType("Void", Void.TYPE, "Used as a replacement for the return type for functions that don't return a value.");
    Type ANY = new SimpleType("Any", Object.class, "Any type.h");


    boolean isAssignableFrom(Type other);


}
