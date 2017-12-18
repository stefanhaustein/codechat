package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.LocalVariableNode;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedIdentifier extends UnresolvedExpression {
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Expression[] EMPTY_EXPRESSION_ARRAY = new Expression[0];
    public final String name;

    public UnresolvedIdentifier(int start, int end, String name) {
        super(start, end);
        this.name = name;
    }

    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        LocalVariable variable = parsingContext.resolve(name);
        if (variable != null) {
            return new LocalVariableNode(variable);
        }
        RootVariable rootVariable = parsingContext.environment.getRootVariable(name);
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

        throw new ExpressionParser.ParsingException(start, end, "Undefined identifier: " + name, null);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append(name);
    }
}
