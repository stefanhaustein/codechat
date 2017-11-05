package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.type.Type;

public class RelationalOperator extends AbstractBinaryOperator {

    public RelationalOperator(char name, Expression left, Expression right) {
        super(name, left, right);
    }

    @Override
    public Object eval(EvaluationContext context) {
        Object l = left.eval(context);
        Object r = right.eval(context);
        switch (name) {
            case '\u2261':
                if (l instanceof TupleInstance) {
                    return l == r;
                }
                // Fallthrough intended.
            case '=':
                return l == null ? r == null : l.equals(r);
            case '\u2260':
                return l == null ? r != null : !l.equals(r);
        }

        int delta = ((Comparable) l).compareTo(r);
        switch (name) {
            case '<': return delta < 0;
            case '>': return delta > 0;
            case '\u2264': return delta <= 0;
            case '\u2265': return delta >= 0;
            default:
                throw new RuntimeException("Impossible");
        }
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public RelationalOperator reconstruct(Expression... children) {
        return new RelationalOperator(name, children[0], children[1]);
    }

}
