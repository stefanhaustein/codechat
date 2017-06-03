package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import org.kobjects.codechat.lang.Builtins;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.lang.RootVariable;

public class Identifier extends AbstractUnresolved {
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Expression[] EMPTY_EXPRESSION_ARRAY = new Expression[0];
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }

    public Expression resolve(ParsingContext parsingContext) {
        LocalVariable variable = parsingContext.resolve(name);
        if (variable != null) {
            return new LocalVariableNode(variable);
        }

        RootVariable rootVariable = parsingContext.environment.rootVariables.get(name);
        if (rootVariable != null) {
            return new RootVariableNode(name, rootVariable.type);
        }

        String methodName = name.equals("continue") ? "unpause" : name;
        for (Object builtins : parsingContext.environment.builtins) {
            Class<?> builtinClass = builtins instanceof Class ? (Class) builtins : builtins.getClass();
            try {
                Method method = builtinClass.getMethod(methodName, EMPTY_CLASS_ARRAY);
                return new BuiltinInvocation(builtins instanceof Class ? null : builtins, method, false, EMPTY_EXPRESSION_ARRAY);
            } catch (NoSuchMethodException e) {
            }
        }
        throw new RuntimeException("Undefined identifier: " + name);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    public void toString(StringBuilder sb, int indent) {
        sb.append(name);
    }

    @Override
    public int getChildCount() {
        return 0;
    }
}
