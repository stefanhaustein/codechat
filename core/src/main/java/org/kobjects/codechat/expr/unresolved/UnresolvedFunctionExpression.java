package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class UnresolvedFunctionExpression extends UnresolvedExpression {
    public Closure closure;
    public Statement body;
    private FunctionType functionType;
    public String[] parameterNames;

    int id;

    public UnresolvedFunctionExpression(int start, int end, int id, FunctionType functionType, String[] parameterNames, Closure closure, Statement body) {
        super(start, end);
        this.id = id;

        this.functionType = functionType;
        this.parameterNames = parameterNames;
        this.closure = closure;
        this.body = body;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        return new FunctionExpression(id, functionType, parameterNames, closure, body);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }


    @Override
    public void toString(AnnotatedStringBuilder asb, int indent) {
        functionType.serializeSignature(asb, id, null, parameterNames, null);
        if (body == null) {
            asb.append(";\n");
        } else {
            asb.append(" :\n");
            body.toString(asb, indent + 1);
            AbstractStatement.indent(asb, indent);
            asb.append("end\n");
        }
    }
}
