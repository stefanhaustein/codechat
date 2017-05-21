package org.kobjects.codechat.lang;


public class FunctionType extends Type {

    public Type returnType;
    Type[] parameterTypes;

    public FunctionType(Type returnType, Type... parameterTypes) {
        super(Function.class);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }
}
