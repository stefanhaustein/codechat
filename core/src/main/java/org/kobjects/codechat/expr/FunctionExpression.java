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

    public static String getQualifiedName(String name, Type... types) {
        StringBuilder sb = new StringBuilder(name);
        for (Type type: types) {
            sb.append(':');
            sb.append(type.toString());
        }
        return sb.toString();
    }


    public Closure closure;
    public Statement body;
    private FunctionType functionType;
    private String[] parameterNames;
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
        UserFunction result = new UserFunction(this, closure.createEvalContext(context));
        if (name != null) {
            context.environment.addFunction(name, result);
        }
        return result;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        return this;
    }

    @Override
    public FunctionType getType() {
        return functionType;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    public void serializeSignature(StringBuilder sb) {
        sb.append("function ");
        if (name != null) {
            sb.append(name);
        }
        sb.append("(");
        for (int i = 0; i < parameterNames.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterNames[i]).append(": ").append(functionType.parameterTypes[i]);
        }
        sb.append("): ").append(functionType.returnType);
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        serializeSignature(sb);
        sb.append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}\n");
    }

    @Override
    public int getChildCount() {
        return 0;
    }
}
