package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Evaluable;

public interface Statement extends Evaluable {
    void toString(StringBuilder sb, int indent);
}
