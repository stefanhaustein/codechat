package org.kobjects.codechat.expr;

import java.util.LinkedHashMap;
import java.util.Map;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class MultiAssignment extends Expression {

    final Expression base;
    final LinkedHashMap<String, Expression> elements;  // TODO: Resolve to properties in ctor already...

    public MultiAssignment(Expression base, LinkedHashMap<String, Expression> elements) {
        this.base = base;
        this.elements = elements;
    }

    @Override
    public Object eval(EvaluationContext context) {
        TupleInstance instance = (TupleInstance) base.eval(context);
        TupleType type = (TupleType) base.getType();
        for (Map.Entry<String,Expression> entry : elements.entrySet()) {
            type.getProperty(entry.getKey()).set(instance, entry.getValue().eval(context));
        }
        return instance;
    }

    @Override
    public Type getType() {
        return base.getType();
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        base.toString(sb, indent);
        sb.append(" ::\n");
        String materializedIndent = Formatting.space(indent + 2);
        for (Map.Entry<String,Expression> entry : elements.entrySet()) {
            sb.append(materializedIndent);
            sb.append(entry.getKey()).append(" = ");
            entry.getValue().toString(sb, indent + 2);
            sb.append(";\n");
        }
        sb.append(Formatting.space(indent));
        sb.append("end;\n");    }

    @Override
    public int getChildCount() {
        return elements.size();
    }


}