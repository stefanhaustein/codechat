package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.UserFunction;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.statement.Statement;

public class FunctionExpression extends Expression {
    private final FunctionType functionType;
    private final String[] parameterNames;
    private final int id;

    private Closure closure;
    private Statement body;


    public FunctionExpression(int id, FunctionType functionType, String[] parameterNames) {
        this.id = id;
        this.functionType = functionType;
        this.parameterNames = parameterNames;
    }


    public void setBody(Closure closure, Statement body) {
        this.closure = closure;
        this.body = body;
    }

    @Override
    public Object eval(EvaluationContext context) {
        UserFunction result = context.environment.getOrCreateInstance(functionType, id);
        result.init(body, closure, parameterNames, closure.createEvalContext(context));
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
    public void toString(AnnotatedStringBuilder sb, int indent) {
        toString(sb, indent, null);
    }

    public void toString(AnnotatedStringBuilder sb, int indent, String name) {
        functionType.serializeSignature(sb, id, name, parameterNames, null);
        sb.append(" :\n");
        body.toString(sb, indent + 2);
        sb.indent(indent);
        sb.append("end\n");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    public void getDependencies(DependencyCollector result) {
        if (body != null) {
            body.getDependencies(result);
        }
    }

    @Override
    public FunctionExpression reconstruct(Expression... children) {
        FunctionExpression result = new FunctionExpression(id, functionType, parameterNames);
        result.setBody(closure, body);
        return result;
    }

}
