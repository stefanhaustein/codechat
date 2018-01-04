package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedConstructor;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedFunctionExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedLiteral;
import org.kobjects.codechat.expr.unresolved.UnresolvedMultiAssignment;
import org.kobjects.codechat.lang.DeclarationException;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.LocalVarDeclarationStatement;
import org.kobjects.codechat.statement.RootVarDeclarationStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.Type;

public class UnresolvedVarDeclarationStatement extends UnresolvedStatement {

    static Type estimateType(ParsingContext parsingContext, UnresolvedExpression expression) {
        if (expression instanceof UnresolvedLiteral) {
            Object value = ((UnresolvedLiteral) expression).value;
            return Environment.typeOf(value);
        }
        if (expression instanceof UnresolvedFunctionExpression) {
            return ((UnresolvedFunctionExpression) expression).functionType;
        }

        UnresolvedExpression potentialCtor = (expression instanceof UnresolvedMultiAssignment)
            ? ((UnresolvedMultiAssignment) expression).base : expression;

        if (potentialCtor instanceof UnresolvedConstructor) {
            UnresolvedConstructor ctor = (UnresolvedConstructor) potentialCtor;
            return parsingContext.environment.resolveInstanceType(ctor.typeName);
        }
        return expression.resolve(parsingContext, null).getType();
    }

    private final boolean constant;
    private final boolean rootLevel;
    private final String variableName;
    private final Type explicitType;
    private final UnresolvedExpression initializer;
    private final String documentation;

    public UnresolvedVarDeclarationStatement(boolean constant, boolean rootLevel, String variableName, Type explicitType, UnresolvedExpression initializer, String documentation) {
        this.constant = constant;
        this.rootLevel = rootLevel;
        this.variableName = variableName;
        this.explicitType = explicitType;
        this.initializer = initializer;
        this.documentation = documentation;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      sb.append(constant ? "let ": "variable ").append(variableName).append(" = ");
      initializer.toString(sb, indent + 4);
      sb.append('\n');
    }

    private Type resolveType(ParsingContext parsingContext) {
        if (explicitType != null) {
            return explicitType;
        }
        if (initializer == null) {
            // Should be caught at parsing already.
            throw new RuntimeException("Intitializer and type can't both be null");
        }
        return estimateType(parsingContext, initializer);
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        Statement result;
        if (rootLevel) {
            RootVariable variable = parsingContext.environment.getRootVariable(variableName);
            variable.documentation = documentation;
            try {
                Expression resolvedInitilaizer = initializer.resolve(parsingContext, explicitType);
                result = new RootVarDeclarationStatement(variable, resolvedInitilaizer, explicitType != null);
            } catch (Exception e) {
                variable.error = e;
                variable.setUnparsed(toString());
                throw new DeclarationException(variable, e);
            }
        } else {
            Expression resolved = initializer.resolve(parsingContext, explicitType);
            LocalVariable variable = parsingContext.addVariable(variableName, resolveType(parsingContext), constant);
            result = new LocalVarDeclarationStatement(variable, resolved);
        }
        return result;
    }

    @Override
    public void resolveTypes(ParsingContext parsingContext) {
        if (rootLevel) {
            parsingContext.environment.declareRootVariable(variableName, resolveType(parsingContext), constant);
        }
    }
}
