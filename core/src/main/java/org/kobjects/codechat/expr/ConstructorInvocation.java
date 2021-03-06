package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.Type;

public class ConstructorInvocation extends Expression {

    Classifier type;
    int id;

    public ConstructorInvocation(Classifier type, int id) {
        if (!type.isInstantiable()) {
            throw new RuntimeException("Type '" + type + "' is not instantiable!");
        }
        this.type = type;
        this.id = id;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return context.environment.createInstance(type, id);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append("new ").append(type.toString());
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public ConstructorInvocation reconstruct(Expression... children) {
        return new ConstructorInvocation(type, id);
    }
}
