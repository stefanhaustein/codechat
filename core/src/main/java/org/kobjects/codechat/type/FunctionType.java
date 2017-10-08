package org.kobjects.codechat.type;


import java.util.List;
import javax.print.Doc;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.lang.Documented;
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
        sb.append(returnType == null ? "func(" : "proc(");
        if (parameterTypes.length > 0) {
            sb.append(parameterTypes[0]);
            for (int i = 1; i < parameterTypes.length; i++) {
                sb.append(", ");
                sb.append(parameterTypes[i]);
            }
        }
        if (returnType != null) {
            sb.append(":").append(returnType);
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
    public UserFunction createInstance(Environment environment, int id) {
        return new UserFunction(this, id);
    }

    public int serializeSignature(AnnotatedStringBuilder asb, int id, String name, String[] parameterNames, EntityLink link) {
        int p0 = asb.length();
        asb.append(returnType == null ? "proc" : "func");
        if (name != null) {
            asb.append(' ').append(name, link);
        } else if (id != -1) {
            asb.append('#').append(id);
            asb.addAnnotation(p0, asb.length(), link);
        }
        int nameEnd = asb.length();
        if (name == null) {
            asb.append(' ');
        }
        asb.append('(');
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                asb.append(", ");
            }
            if (parameterNames != null) {
                asb.append(parameterNames[i]).append(": ");
            }
            asb.append(parameterTypes[i].toString(), parameterTypes[i] instanceof Documented ? new DocumentedLink((Documented) parameterTypes[i]) : null);
        }
        asb.append(')');
        if (returnType != null) {
            asb.append(": ");
            asb.append(returnType.toString(), returnType instanceof Documented ? new DocumentedLink((Documented) returnType) : null);
        }
        return nameEnd;
    }
}
