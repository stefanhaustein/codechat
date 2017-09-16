package org.kobjects.codechat.expr;

import java.util.LinkedHashMap;
import java.util.Map;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class ObjectLiteral extends Expression {

    final TupleType type;
    final int id;
    final LinkedHashMap<String, Expression> elements;

    public ObjectLiteral(TupleType type, int id, LinkedHashMap<String, Expression> elements) {
        this.type = type;
        this.id = id;
        this.elements = elements;
    }

    @Override
    public Object eval(EvaluationContext context) {
        TupleInstance instance = (TupleInstance) context.environment.instantiate(type, id);
        for (Map.Entry<String,Expression> entry : elements.entrySet()) {
            type.getProperty(entry.getKey()).set(instance, entry.getValue().eval(context));
        }
        return instance;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(type.getName());
        if (id != -1) {
            sb.append('#').append(id);
        }
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String,Expression> entry : elements.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append(": ");
            entry.getValue().toString(sb, indent + 1);
        }
        sb.append('}');
    }

    @Override
    public int getChildCount() {
        return elements.size();
    }


}
