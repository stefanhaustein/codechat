package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.unresolved.UnresolvedConstructor;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Block;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.InstanceType;

public class UnresolvedBlock extends UnresolvedStatement {
    public UnresolvedStatement[] statements;

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

    @Override
    public void prepareInstances(ParsingContext parsingContext) {
        for (UnresolvedStatement statement : statements) {
            statement.prepareInstances(parsingContext);
        }
    }

}
