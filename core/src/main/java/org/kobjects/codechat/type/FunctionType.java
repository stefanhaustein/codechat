package org.kobjects.codechat.type;


import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.UserFunction;

public class FunctionType extends InstanceType<UserFunction> {

    public final Type returnType;
    public final Type[] parameterTypes;

    public FunctionType(Type returnType, Type... parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append("lambda(");
        if (parameterTypes.length > 0) {
            sb.append(parameterTypes[0]);
            for (int i = 1; i < parameterTypes.length; i++) {
                sb.append(", ");
                sb.append(parameterTypes[i]);
            }
        }
        if (returnType != null) {
            sb.append(" -> ").append(returnType);
        }
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
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public UserFunction createInstance(Environment environment) {
        return new UserFunction(environment, this);
    }

    public int serializeSignature(AnnotatedStringBuilder asb, int id, String name, String[] parameterNames, InstanceLink link) {
        if (name != null) {
            asb.append("def ").append(name, link);
        } else if (id != -1) {
            asb.append("lambda#" + id, link);
        }
        int nameEnd = asb.length();
        if (name == null) {
            asb.append(' ');
        }
        serializeSignature(asb, parameterNames);
        return nameEnd;
    }

    public void serializeSignature(AnnotatedStringBuilder asb, String[] parameterNames) {
        asb.append('(');
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                asb.append(", ");
            }
            if (parameterNames != null) {
                asb.append(parameterNames[i]).append(": ");
            }
            asb.append(parameterTypes[i].getName(), new DocumentedLink(parameterTypes[i]));
        }
        asb.append(')');
        if (returnType != null) {
            asb.append(" -> ");
            asb.append(returnType.getName(), new DocumentedLink(returnType));
        }
    }
}
