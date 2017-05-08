package org.kobjects.codechat.statement;

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
            statements[i].eval(context);
        }
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        for (Statement statement: statements) {
            statement.toString(sb, indent);
        }
    }
}
