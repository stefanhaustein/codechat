package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.Type;

public class RootVariable {
    public String name;
    public Type type;
    public Object value;

    public String dump(boolean detailed) {
        StringBuilder sb = new StringBuilder();

        boolean namedFunction = value instanceof UserFunction && ((UserFunction) value).isNamed();
        if (!namedFunction) {
            sb.append(name);
            sb.append(" = ");
        }

        if (namedFunction && !detailed) {
            ((UserFunction) value).serializeSignature(sb);
        } else {
            sb.append(Environment.toLiteral(value));
        }
        if (!namedFunction || !detailed) {
            sb.append(";\n");
        }
        return sb.toString();
    }
}
