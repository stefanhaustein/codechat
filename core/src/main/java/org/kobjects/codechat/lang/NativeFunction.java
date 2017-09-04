package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public abstract class NativeFunction implements Function, Documented {
    private final FunctionType type;
    public final String name;
    private final String documentation;

    public NativeFunction(String name, Type returnType, String documentation, Type... parameterTypes) {
        this.type = new FunctionType(returnType, parameterTypes);
        this.name = name;
        this.documentation = documentation;
    }

    @Override
    public EvaluationContext createContext() {
        return new EvaluationContext(null, type.parameterTypes.length);
    }

    @Override
    public Object eval(EvaluationContext functionContext) {
        return eval(functionContext.variables);
    }

    protected abstract Object eval(Object[] params);

    @Override
    public FunctionType getType() {
        return type;
    }

    @Override
    public AnnotatedCharSequence getDocumentation() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        type.serializeSignature(asb.getStringBuilder(), -1, name, null, asb.getAnnotationList());
        asb.append("\n");
        asb.append(documentation);
        return asb.build();
    }
}
