package org.kobjects.codechat.expr;

import java.util.ArrayList;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Function;
import org.kobjects.codechat.lang.FunctionType;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class FunctionExpr extends Expression {

    ArrayList<Param> params = new ArrayList<>();
    private Closure closure;
    private Statement body;
    private FunctionType type;

    @Override
    public Object eval(EvaluationContext context) {
        return new Function(this);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        return this;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append("function (");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params.get(i).name).append(": ").append(params.get(i).type);
        }
        sb.append("): ").append(type.returnType).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}");
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
