package org.kobjects.codechat.statement;

import java.util.Collection;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;

public class Block extends AbstractStatement {
    Statement[] statements;

    public Block(Statement[] statements) {
        this.statements = statements;
    }

    @Override
    public Object eval(EvaluationContext context) {
        int count = statements.length;
        for (int i = 0; i < statements.length; i++) {
            Object result = statements[i].eval(context);
            if (result != KEEP_GOING) {
                return result;
            }
        }
        return KEEP_GOING;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        for (Statement statement: statements) {
            statement.toString(sb, indent);
        }
    }

    @Override
    public void getDependencies(Environment environment, Collection<Entity> result) {
        for (Statement statement: statements) {
            statement.getDependencies(environment, result);
        }
    }
}
