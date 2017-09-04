package org.kobjects.codechat.type;


import java.util.List;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.lang.Environment;
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
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public UserFunction createInstance(Environment environment, int id) {
        return new UserFunction(this, id);
    }

    public int serializeSignature(StringBuilder sb, int id, String name, String[] parameterNames, List<AnnotationSpan> annotations) {
        sb.append("function");
        if (id != -1) {
            sb.append('#').append(id);
        }
        if (name != null) {
            sb.append(' ');
            sb.append(name);
        }
        int nameEnd = sb.length();
        if (name == null) {
            sb.append(' ');
        }
        sb.append("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (parameterNames != null) {
                sb.append(parameterNames[i]).append(": ");
            }
            AnnotationSpan.append(sb, parameterTypes[i].toString(), parameterTypes[i], annotations);
        }
        sb.append("): ");
        AnnotationSpan.append(sb, returnType.toString(), returnType, annotations);
        return nameEnd;
    }

}
