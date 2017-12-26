package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;

public class IfStatement extends AbstractStatement {
    public Expression condition;
    public Statement ifBody;
    public Statement elseBody;

    public IfStatement(Expression condition, Statement ifBody, Statement elseBody) {
        this.condition = condition;
        this.ifBody = ifBody;
        this.elseBody = elseBody;
    }

    @Override
    public Object eval(EvaluationContext context) {
        if (Boolean.TRUE.equals(condition.eval(context))) {
            ifBody.eval(context);
        } else if (elseBody != null) {
            elseBody.eval(context);
        }
        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        toString(sb, indent, false);
    }

    public void toString(AnnotatedStringBuilder sb, int indent, boolean elseif) {
        if (elseif) {
            sb.append("elseif ");
        } else {
          sb.indent(indent);
          sb.append("if ");
        }
        condition.toString(sb, indent);
        sb.append(":\n");
        ifBody.toString(sb, indent + 2);
      sb.indent(indent);
      if (elseBody instanceof IfStatement) {
            ((IfStatement) elseBody).toString(sb, indent, true);
        } else if (elseBody != null) {
            sb.append("else:\n");
            elseBody.toString(sb, indent + 2);
        sb.indent(indent);
      }
        sb.append("end\n");
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        condition.getDependencies(result);
        ifBody.getDependencies(result);
        if (elseBody != null) {
            elseBody.getDependencies(result);
        }
    }
}
