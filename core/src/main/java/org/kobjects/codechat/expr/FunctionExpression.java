package org.kobjects.codechat.expr;

import java.util.ArrayList;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.UserFunction;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class FunctionExpression extends Expression {
    public Closure closure;
    public Statement body;
    private FunctionType functionType;
    public String[] parameterNames;
    public String name;
    int id;

    public FunctionExpression(int id, String name, FunctionType functionType, String[] parameterNames, Closure closure, Statement body) {
        this.id = id;
        this.name = name;
        this.functionType = functionType;
        this.parameterNames = parameterNames;
        this.closure = closure;
        this.body = body;
    }

    @Override
    public Object eval(EvaluationContext context) {
        UserFunction result;
        if (body == null || id == -1) {
            result = (UserFunction) context.environment.instantiate(functionType, id);
        } else {
            result = (UserFunction) context.environment.getInstance(functionType, id, false);
        }
        result.init(this, closure.createEvalContext(context));
        if (name != null) {
            context.environment.addFunction(name, result);
        }
        return result;
    }

    @Override
    public FunctionType getType() {
        return functionType;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }


    @Override
    public void toString(StringBuilder sb, int indent) {
        functionType.serializeSignature(sb, id, name, parameterNames, null);
        if (body == null) {
            sb.append(";\n");
        } else {
            sb.append(" :\n");
            body.toString(sb, indent + 1);
            AbstractStatement.indent(sb, indent);
            sb.append("end\n");
        }
    }

    @Override
    public int getChildCount() {
        return 0;
    }
}
