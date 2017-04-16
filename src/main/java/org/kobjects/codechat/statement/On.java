package org.kobjects.codechat.statement;

import org.kobjects.codechat.Environment;
import org.kobjects.codechat.Evaluable;
import org.kobjects.codechat.Instance;
import org.kobjects.codechat.Ticking;
import org.kobjects.codechat.expr.Node;

public class On extends Instance implements Ticking, Evaluable {
    Environment environment;
    public Node condition;
    public Node exec;

    public On(Environment environment, int id, Node condition, Node exec) {
        super(environment, id);
        this.environment = environment;
        this.condition = condition;
        this.exec = exec;
    }

    @Override
    public void tick(boolean force) {
        if (Boolean.TRUE.equals(condition.eval(environment))) {
            exec.eval(environment);
        }
    }

    public String toString() {
        return "on#" + id + ' ' + condition + ": " + exec;
    }

    @Override
    public Object eval(Environment environment) {
        environment.ticking.add(this);
        return null;
    }
}
