package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.EvaluationContext;

public interface Statement {
    Object eval(EvaluationContext context);
    void toString(StringBuilder sb, int indent);
}
