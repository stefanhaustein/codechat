package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.LocalVariableNode;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Type;

public class UnresolvedIdentifier extends UnresolvedExpression {
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Expression[] EMPTY_EXPRESSION_ARRAY = new Expression[0];
    public final String name;

    public UnresolvedIdentifier(String name) {
        this.name = name;
    }

    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        LocalVariable variable = parsingContext.resolve(name);
        if (variable != null) {
            return new LocalVariableNode(variable);
        }
        RootVariable rootVariable = parsingContext.environment.rootVariables.get(name);
        if (rootVariable != null) {
            return new RootVariableNode(rootVariable);
        }
        if (expectedType instanceof EnumType) {
            try {
                EnumLiteral enumLiteral = ((EnumType) expectedType).getValue(name);
                return new Literal(enumLiteral);
            } catch (Exception e) {

            }
        }

        throw new RuntimeException("Undefined identifier: " + name);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    public void toString(StringBuilder sb, int indent) {
        sb.append(name);
    }


}
