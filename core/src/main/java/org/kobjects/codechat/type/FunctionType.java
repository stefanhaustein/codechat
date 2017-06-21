package org.kobjects.codechat.type;


import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.UserFunction;

public class FunctionType extends Type {

    public final Type returnType;
    public final Type[] parameterTypes;

    public FunctionType(Type returnType, Type... parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append("function(");
        if (parameterTypes.length > 0) {
            sb.append(parameterTypes[0]);
            for (int i = 1; i < parameterTypes.length; i++) {
                sb.append(", ");
                sb.append(parameterTypes[i]);
            }
        }
        sb.append(":").append(returnType);
        return sb.toString();
    }

    public boolean isAssignableFrom(Type other) {
        if (!(other instanceof FunctionType)) {
            return false;
        }
        FunctionType otherType = (FunctionType) other;
        if (!returnType.isAssignableFrom(otherType.returnType) ||
                parameterTypes.length != otherType.parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!otherType.parameterTypes[i].isAssignableFrom(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Class<?> getJavaClass() {
        return UserFunction.class;
    }

}
