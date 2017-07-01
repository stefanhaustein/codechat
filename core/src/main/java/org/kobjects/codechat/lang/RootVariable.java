package org.kobjects.codechat.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.kobjects.codechat.type.Type;

public class RootVariable {
    public String name;
    public Type type;
    public Object value;

    public void dump(StringBuilder sb) {
        sb.append(name);
        sb.append(" = ");
        sb.append(Formatting.toLiteral(value));
        sb.append(";\n");
    }

}
