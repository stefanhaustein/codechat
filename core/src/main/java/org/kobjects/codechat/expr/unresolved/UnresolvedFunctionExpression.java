package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class UnresolvedFunctionExpression extends UnresolvedExpression {
    public Closure closure;
    public Statement body;
    private FunctionType functionType;
    public String[] parameterNames;
    public String name;
    int id;

    public UnresolvedFunctionExpression(int id, String name, FunctionType functionType, String[] parameterNames, Closure closure, Statement body) {
        this.id = id;
        this.name = name;
        this.functionType = functionType;
        this.parameterNames = parameterNames;
        this.closure = closure;
        this.body = body;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        return new FunctionExpression(id, name, functionType, parameterNames, closure, body);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }


    @Override
    public void toString(StringBuilder sb, int indent) {
        functionType.serializeSignature(sb, id, name, parameterNames);
        if (body == null) {
            sb.append(";\n");
        } else {
            sb.append(" :\n");
            body.toString(sb, indent + 1);
            AbstractStatement.indent(sb, indent);
            sb.append("end\n");
        }
    }
}
