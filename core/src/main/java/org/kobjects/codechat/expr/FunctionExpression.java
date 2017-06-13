package org.kobjects.codechat.expr;

import java.util.ArrayList;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.UserFunction;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.RootVariable;
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


    ArrayList<Param> params = new ArrayList<>();
    public Closure closure;
    public Statement body;
    private FunctionType type;
    public String name;

    public FunctionExpression(String name) {
        this.name = name;
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
        return type;
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
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params.get(i).name).append(": ").append(params.get(i).type);
        }
        sb.append("): ").append(type.returnType);
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

    public void addParam(String name, Type type) {
        params.add(new Param(name, type));
    }

    public void init(Type returnType, Closure closure, Statement body) {
        this.closure = closure;
        this.body = body;

        Type[] paramTypes = new Type[params.size()];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = params.get(i).type;
        }
        this.type = new FunctionType(returnType, paramTypes);
    }



    static class Param {
        final String name;
        final Type type;
        Param(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }
}
