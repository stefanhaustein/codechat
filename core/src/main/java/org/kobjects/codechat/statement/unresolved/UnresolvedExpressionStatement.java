package org.kobjects.codechat.statement.unresolved;

import java.util.ArrayList;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionInvocation;
import org.kobjects.codechat.expr.unresolved.UnresolvedBinaryOperator;
import org.kobjects.codechat.expr.unresolved.UnresolvedConstructor;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedIdentifier;
import org.kobjects.codechat.expr.unresolved.UnresolvedInvocation;
import org.kobjects.codechat.expr.unresolved.UnresolvedMultiAssignment;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.InstanceType;

public class UnresolvedExpressionStatement extends  UnresolvedStatement {
    private final UnresolvedExpression expression;
    private final boolean interactive;

    public UnresolvedExpressionStatement(UnresolvedExpression expression) {
        this(expression, false);
    }

    public UnresolvedExpressionStatement(UnresolvedExpression expression, boolean interactive) {
        this.expression = expression;
        this.interactive = interactive;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        expression.toString(sb, indent + 4);
        sb.append("\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        Expression resolved = expression.resolve(parsingContext, null);
        if (resolved.getType() instanceof FunctionType) {
           return new ExpressionStatement(new FunctionInvocation(resolved, false));
        }
        return new ExpressionStatement(resolved);
    }

    @Override
    public void prepareInstances(ParsingContext parsingContext) {
        UnresolvedExpression potentialCtor = (expression instanceof UnresolvedMultiAssignment)
                ? ((UnresolvedMultiAssignment) expression).base : expression;
        if (potentialCtor instanceof UnresolvedConstructor) {
            UnresolvedConstructor ctor = (UnresolvedConstructor) potentialCtor;
            if (ctor.id != -1) {
                // Needed because there may be references before it's instantiated
                // Alternatives might be to just allow implicit creation or going back to 2-phase init
                InstanceType type = (InstanceType) parsingContext.environment.resolveType(ctor.typeName);
                parsingContext.environment.getInstance(type, ctor.id, true);
            }
        }
    }
}
