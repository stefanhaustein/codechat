package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import org.kobjects.codechat.lang.Builtins;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.lang.RootVariable;

public class Identifier extends AbstractUnresolved {
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

        try {
            Method method = Builtins.class.getMethod(name.equals("continue") ? "unpause" : name);
            return new BuiltinInvocation(method, false);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Undefined identifier: " + name);
        }
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
