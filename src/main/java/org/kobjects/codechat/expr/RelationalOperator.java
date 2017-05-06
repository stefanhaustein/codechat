package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class RelationalOperator extends Expression {

    public char name;

    public Expression left;
    public Expression right;

    public RelationalOperator(char name, Expression left, Expression right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(Context context) {
        Object l = left.eval(context);
        Object r = right.eval(context);
        switch (name) {
            case '\u2261':
                if (l instanceof Instance) {
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
    public Expression resolve(Scope scope) {
        left = left.resolve(scope);
        right = right.resolve(scope);
        return this;
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_RELATIONAL;
    }

    @Override
    public void toString(StringBuilder sb) {
        left.toString(sb, Parser.PRECEDENCE_RELATIONAL);
        sb.append(' ');
        sb.append(name);
        sb.append(' ');
        right.toString(sb, Parser.PRECEDENCE_RELATIONAL);
    }

    @Override
    public int getChildCount() {
        return 2;
    }

    @Override
    public Expression getChild(int index) {
        return index == 0 ? left : right;
    }
}
