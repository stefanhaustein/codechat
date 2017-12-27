package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedIdentifier;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Assignment;
import org.kobjects.codechat.statement.Statement;

public class UnresolvedAssignment extends UnresolvedStatement {
    UnresolvedExpression left;
    UnresolvedExpression right;
    UnresolvedVarDeclarationStatement delegate;

    public UnresolvedAssignment(UnresolvedExpression left, UnresolvedExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      left.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append(" = ");
        right.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append("\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
      if (delegate != null) {
        return delegate.resolve(parsingContext);
      }
      Expression resolvedTarget = left.resolve(parsingContext, null);
      return new Assignment(resolvedTarget, right.resolve(parsingContext, resolvedTarget.getType()));
    }

    @Override
    public void resolveTypes(ParsingContext parsingContext) {
      // Root level is implied here.
      if (parsingContext.mode != ParsingContext.Mode.LOAD && left instanceof UnresolvedIdentifier) {
        String name = ((UnresolvedIdentifier) left).name;
        RootVariable variable = parsingContext.environment.getRootVariable(name);
        if (variable == null || variable.constant ||
            !variable.type.isAssignableFrom(
                UnresolvedVarDeclarationStatement.estimateType(parsingContext, right))) {
          delegate = new UnresolvedVarDeclarationStatement(true, true, name, null, right, null);
          delegate.resolveTypes(parsingContext);
        }
      }
    }

}
