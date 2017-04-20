package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import org.kobjects.codechat.api.Builtins;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Variable;

public class Identifier extends Unresolved {
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }

    public Expression resolve(Scope scope) {
        Variable variable = scope.resolve(name);
        if (variable != null) {
            return new VariableNode(variable);
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

    public void toString(StringBuilder sb) {
        sb.append(name);
    }
}
