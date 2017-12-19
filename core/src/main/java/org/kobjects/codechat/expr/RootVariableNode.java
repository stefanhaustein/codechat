package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.Type;

public class RootVariableNode extends Expression {
    public final RootVariable rootVariable;

    public RootVariableNode(RootVariable rootVariable) {
        this.rootVariable = rootVariable;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return rootVariable.value;
    }

    @Override
    public Type getType() {
        return rootVariable.type;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append(rootVariable.name);
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public Object getLock(EvaluationContext context) {
        return rootVariable;
    }

    @Override
    public void assign(EvaluationContext context, Object value) {
        rootVariable.value = value;

    }


    public boolean isAssignable() {
        return !rootVariable.constant;
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        result.add(rootVariable);
    }

    @Override
    public RootVariableNode reconstruct(Expression... children) {
        return new RootVariableNode(rootVariable);
    }
}