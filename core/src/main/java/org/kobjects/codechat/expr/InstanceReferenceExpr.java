package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.Type;

public class InstanceReferenceExpr extends Expression {
    public final int id;
    public final Classifier type;

    public InstanceReferenceExpr(Classifier type, int id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return context.environment.getInstance(type, id);
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
    public void toString(AnnotatedStringBuilder asb, int indent) {
        asb.append(type.toString()).append('#').append(id);
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        result.add(result.getEnvironment().getInstance(type, id));
    }

    @Override
    public InstanceReferenceExpr reconstruct(Expression... children) {
        return new InstanceReferenceExpr(type, id);
    }
}
