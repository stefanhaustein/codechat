package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Function;
import org.kobjects.codechat.lang.UserMethod;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class MethodAccess extends Expression {

    private final UserMethod method;
    private final Expression base;

    public MethodAccess(Expression base, UserMethod method) {
        this.base = base;
        this.method = method;
    }

    @Override
    public Object eval(final EvaluationContext context) {
        return new Function() {
            @Override
            public EvaluationContext createContext() {
                return new EvaluationContext(context.environment, method.getType().parameterTypes.length);
            }

            @Override
            public Object eval(EvaluationContext functionContext) {
                return method.eval(functionContext);
            }

            @Override
            public FunctionType getType() {
                return method.getType();
            }
        };
    }

    @Override
    public Type getType() {
        return method.getType();
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, int indent) {
        base.toString(asb, indent, Parser.PRECEDENCE_PATH);
        asb.append('.');
        asb.append(method.name);
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public Expression getChild(int index) {
        return base;
    }

    @Override
    public MethodAccess reconstruct(Expression... children) {
        return new MethodAccess(children[0], method);
    }
}
