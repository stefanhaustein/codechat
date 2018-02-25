package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.UserClassifier;

public class SelfExpr extends Expression {
    private final UserClassifier classType;
    public SelfExpr(UserClassifier classType) {
        this.classType = classType;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return context.self;
    }

    @Override
    public Type getType() {
        return classType;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, int indent) {
        asb.append("self");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public Expression reconstruct(Expression... children) {
        return new SelfExpr(classType);
    }
}
