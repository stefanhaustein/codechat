package org.kobjects.codechat.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.kobjects.codechat.type.Type;

public class RootVariable {
    public String name;
    public Type type;
    public Object value;
    List<Function> functions;

    public void dump(StringBuilder sb) {
        sb.append(name);
        sb.append(" = ");
        sb.append(Formatting.toLiteral(value));
        sb.append(";\n");
    }

    public Iterable<Function> functions() {
        return functions == null ? Collections.<Function>emptyList() : functions;
    }

    public void addFunction(Function function) {
        if (functions == null) {
            functions = new ArrayList<>();
        }

        for (int i = 0; i < functions.size(); i++) {
            Function candidate = functions.get(i);
            if (candidate.getType().callScore(function.getType().parameterTypes) == 1.0) {
                // TODO: Should be identical!
                functions.set(i, function);
                return;
            }
        }
        functions.add(function);
    }
}
