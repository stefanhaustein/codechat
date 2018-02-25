package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionDeclaration;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.statement.unresolved.UnresolvedStatement;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.unresolved.UnresolvedFunctionSignature;

public class UnresolvedFunctionExpression extends UnresolvedExpression {
    public final UnresolvedStatement body;
    public final UnresolvedFunctionSignature signature;
    public final int id;

    public UnresolvedFunctionExpression(int start, int end, int id, UnresolvedFunctionSignature signature, UnresolvedStatement body) {
        super(start, end);
        this.id = id;

        this.signature = signature;
        this.body = body;
    }

    @Override
    public Expression resolve(final ParsingContext parsingContext, Type expectedType) {
        final FunctionDeclaration result = new FunctionDeclaration(id, signature.resolve(parsingContext),
                signature.parameterNames.toArray(new String[signature.parameterNames.size()]));

        parsingContext.enqueue(new Runnable() {
            @Override
            public void run() {
                ParsingContext bodyContext = new ParsingContext(parsingContext, true);
                for (int i = 0; i < signature.parameterNames.size(); i++) {
                    bodyContext.addVariable(signature.parameterNames.get(i),
                            signature.parameterTypes.get(i).resolve(parsingContext), true);
                }
                Statement resolvedBody = body.resolve(bodyContext);
                result.setBody(bodyContext.getClosure(), resolvedBody);
            }
        });

        return result;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }


    @Override
    public void toString(AnnotatedStringBuilder asb, int indent) {
        if (id == -1) {
            asb.append("func");
        } else {
            asb.append("func#").append(id);
        }
        signature.print(asb);
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
