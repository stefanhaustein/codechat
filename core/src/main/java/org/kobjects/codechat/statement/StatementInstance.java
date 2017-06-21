package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.TupleInstance;

public abstract class StatementInstance extends TupleInstance implements Statement {

    protected StatementInstance(Environment environment, int id) {
        super(environment, id);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

}
