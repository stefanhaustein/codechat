package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.HasDependencies;

public interface Statement extends HasDependencies {
    Object eval(EvaluationContext context);
    void toString(AnnotatedStringBuilder sb, int indent);
}
