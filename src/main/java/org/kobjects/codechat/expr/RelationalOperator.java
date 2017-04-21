package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class RelationalOperator extends Expression {

    char name;

    Expression left;
    Expression right;

    public RelationalOperator(String name, Expression left, Expression right) {

        if (name.length() == 1) {
            this.name = name.charAt(0);
        } else {
            switch (name) {
                case "<=": this.name = '\u2264'; break;
                case ">=": this.name = '\u2265'; break;
                case "!=": this.name = '\u2260'; break;
                case "==": this.name = '='; break;
                default:
                    throw new RuntimeException("impossible");
            }
        }

        this.left = left;
        this.right = right;
    }


    @Override
    public Object eval(Context context) {
        Object l = left.eval(context);
        Object r = right.eval(context);
        if (name == '=') {
            return l == null ? r == null : l.equals(r);
        }
        if (name == '\u2260') {
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
        if (name == '=') {
            sb.append('=');
        }
        sb.append(' ');
        right.toString(sb, Parser.PRECEDENCE_RELATIONAL);
    }
}
