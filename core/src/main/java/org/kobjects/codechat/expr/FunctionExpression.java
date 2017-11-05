package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.UserFunction;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class FunctionExpression extends Expression {
    public Closure closure;
    public Statement body;
    private FunctionType functionType;
    public String[] parameterNames;
    int id;

    public FunctionExpression(int id, FunctionType functionType, String[] parameterNames, Closure closure, Statement body) {
        this.id = id;
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
            result = (UserFunction) context.environment.getInstance(functionType, id, true);
        }
        result.init(this, closure.createEvalContext(context));
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

    public void getDependencies(DependencyCollector result) {
        if (body != null) {
            body.getDependencies(result);
        }
    }

    @Override
    public FunctionExpression reconstruct(Expression... children) {
        return new FunctionExpression(id, functionType, parameterNames, closure, body);
    }

}
