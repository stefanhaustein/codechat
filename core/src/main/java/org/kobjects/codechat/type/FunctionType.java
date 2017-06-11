package org.kobjects.codechat.type;


import org.kobjects.codechat.lang.Function;

public class FunctionType extends Type {

    public final Type returnType;
    public final Type[] parameterTypes;

    public FunctionType(Type returnType, Type... parameterTypes) {
        super(Function.class);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }
}
