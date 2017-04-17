package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Evaluable;

public class Block extends AbstractStatement {
    Evaluable[] statements;

    public Block(Evaluable[] statements) {
        this.statements = statements;
    }

    @Override
    public Object eval(Environment environment) {
        int count = statements.length;
        for (int i = 0; i < statements.length; i++) {
            statements[i].eval(environment);
        }
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        for (Evaluable e: statements) {
            if (e instanceof Block) {
                sb.append(e.toString());
                //                ((Block) e).toString(sb, indent);
            } else {
                sb.append(e.toString());
                sb.append("; ");
                /*   for (int i = 0; i < indent; i++) {
                    sb.append(' ');
                }
                sb.append(e.toString());
                sb.append('\n'); */
            }
        }
    }
}
