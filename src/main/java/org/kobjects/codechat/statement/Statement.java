package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Context;

public interface Statement {
    Object eval(Context context);
    void toString(StringBuilder sb, int indent);
}
