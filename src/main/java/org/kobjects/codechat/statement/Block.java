package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Evaluable;

public class Block extends AbstractStatement {
    Evaluable[] statements;

    public Block(Evaluable[] statements) {
        this.statements = statements;
    }

    @Override
    public Object eval(Context context) {
        int count = statements.length;
        for (int i = 0; i < statements.length; i++) {
            statements[i].eval(context);
        }
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        for (Evaluable e: statements) {
            if (e instanceof Statement) {
                ((Statement) e).toString(sb, indent);
            } else {
                indent(sb, indent);
                sb.append(e.toString());
                sb.append(";\n");
            }
        }
    }
}
