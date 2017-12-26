package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.statement.unresolved.UnresolvedStatement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class UnresolvedFunctionExpression extends UnresolvedExpression {
    public final UnresolvedStatement body;
    public final FunctionType functionType;
    public final String[] parameterNames;
    public final int id;

    public UnresolvedFunctionExpression(int start, int end, int id, FunctionType functionType, String[] parameterNames, UnresolvedStatement body) {
        super(start, end);
        this.id = id;

        this.functionType = functionType;
        this.parameterNames = parameterNames;
        this.body = body;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        ParsingContext bodyContext = new ParsingContext(parsingContext, true);
        for (int i = 0; i < parameterNames.length; i++) {
            bodyContext.addVariable(parameterNames[i], functionType.parameterTypes[i], true);
        }
        Statement resolvedBody = body.resolve(bodyContext);
        return new FunctionExpression(id, functionType, parameterNames, bodyContext.getClosure(), resolvedBody);
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
            body.toString(asb, indent + 2);
            asb.indent(indent);
            asb.append("end\n");
        }
    }
}
