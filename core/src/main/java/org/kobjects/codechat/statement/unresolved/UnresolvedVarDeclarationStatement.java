package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.expr.unresolved.UnresolvedConstructor;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedFunctionExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedLiteral;
import org.kobjects.codechat.expr.unresolved.UnresolvedMultiAssignment;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Assignment;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.LocalVarDeclarationStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedVarDeclarationStatement extends UnresolvedStatement {
    private final boolean constant;
    private final boolean rootLevel;
    private final String variableName;
    private final Type type;
    private final UnresolvedExpression initializer;
    private final String documentation;

    public UnresolvedVarDeclarationStatement(boolean constant, boolean rootLevel, String variableName, Type type, UnresolvedExpression initializer, String documentation) {
        this.constant = constant;
        this.rootLevel = rootLevel;
        this.variableName = variableName;
        this.type = type;
        this.initializer = initializer;
        this.documentation = documentation;
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
        Expression resolved;
        Type resolvedType;
        if (initializer == null) {
            resolved = null;
            resolvedType = type;
        } else {
            resolved = initializer.resolve(parsingContext, type);
            if (type != null && !type.isAssignableFrom(resolved.getType())) {
                throw new ExpressionParser.ParsingException(initializer.start, initializer.end, "Declared type '" + type + "' not assignable from expression.", null);
            }
            resolvedType = type == null ? resolved.getType() : type;
        }

        if (rootLevel) {
            if (resolvedType == null) {
                throw new RuntimeException(
                        "Explicit type or initializer required for root constants and variables: '" + variableName + "'", null);
            }
            RootVariable rootVariable = parsingContext.environment.declareRootVariable(variableName, resolvedType, constant);
            rootVariable.documentation = documentation;
            Expression left = new RootVariableNode(rootVariable);
            if (resolved == null) {
                return new ExpressionStatement(left);
            }
            return new Assignment(left, resolved);
        }

        if (resolved == null) {
            throw new RuntimeException(
                    "Initializer required for local constants and variables: '" + variableName + "'.", null);
        }
        LocalVariable variable = parsingContext.addVariable(variableName, resolvedType, constant);
        return new LocalVarDeclarationStatement(variable, resolved);
    }

    @Override
    public void prepareInstances(ParsingContext parsingContext) {
        if (rootLevel) {
            Type resolvedType = type;
            if (type == null) {
                if (initializer instanceof UnresolvedLiteral) {
                    Object value = ((UnresolvedLiteral) initializer).value;
                    resolvedType = Type.of(value);
                } else if (initializer instanceof UnresolvedFunctionExpression) {
                    resolvedType = ((UnresolvedFunctionExpression) initializer).functionType;
                } else {
                    UnresolvedExpression potentialCtor = (initializer instanceof UnresolvedMultiAssignment)
                            ? ((UnresolvedMultiAssignment) initializer).base : initializer;

                    if (potentialCtor instanceof UnresolvedConstructor) {
                        UnresolvedConstructor ctor = (UnresolvedConstructor) potentialCtor;
                        resolvedType = parsingContext.environment.resolveType(ctor.typeName);
                    }
                }
            }
            if (resolvedType != null) {
                parsingContext.environment.declareRootVariable(variableName, resolvedType, constant);
            }
        }
    }
}
