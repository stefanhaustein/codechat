package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;

public class Scope extends AbstractStatement {
    private final Statement body;

    public Scope(Statement body) {
        this.body = body;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return body.eval(context);
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      sb.append("scope\n");
        body.toString(sb, indent + 2);
      sb.indent(indent);
      sb.append("end\n");
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        body.getDependencies(result);
    }
}
