package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Block;
import org.kobjects.codechat.statement.Statement;

public class UnresolvedBlock extends UnresolvedStatement {
    UnresolvedStatement[] statements;

    public UnresolvedBlock(UnresolvedStatement[] statements) {
        this.statements = statements;
    }


    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        for (UnresolvedStatement statement: statements) {
            statement.toString(sb, indent);
        }
    }

    @Override
    public Block resolve(ParsingContext parsingContext) {
        Statement[] resolved = new Statement[statements.length];
        for (int i = 0; i < resolved.length; i++) {
            resolved[i] = statements[i].resolve(parsingContext);
        }
        return new Block(resolved);
    }

}
