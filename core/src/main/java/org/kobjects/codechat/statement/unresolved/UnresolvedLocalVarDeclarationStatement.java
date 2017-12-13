package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class UnresolvedLocalVarDeclarationStatement extends UnresolvedStatement {
    boolean constant;
    String variableName;
    UnresolvedExpression initializer;

    public UnresolvedLocalVarDeclarationStatement(boolean constant, String variableName, UnresolvedExpression initializer) {
        this.constant = constant;
        this.variableName = variableName;
        this.initializer = initializer;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append(constant ? "let ": "variable ").append(variableName).append(" = ");
        initializer.toString(sb, indent + 4);
        sb.append("\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        throw new RuntimeException("NYI");
    }

}
