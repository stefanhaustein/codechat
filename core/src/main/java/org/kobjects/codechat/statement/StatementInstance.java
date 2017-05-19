package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;

public abstract class StatementInstance extends Instance implements Statement {

    protected StatementInstance(Environment environment, int id) {
        super(environment, id);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

}
