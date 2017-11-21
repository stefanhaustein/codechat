package org.kobjects.codechat.expr;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.type.InstanceType;
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
        Instance instance = (Instance) base.eval(context);
        InstanceType type = (InstanceType) base.getType();
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
    public void toString(AnnotatedStringBuilder sb, int indent) {
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
        return 1 + elements.size();
    }

    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return base;
        }
        Iterator<Expression> iterator = elements.values().iterator();
        for (int i = 1; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    @Override
    public MultiAssignment reconstruct(Expression... children) {
        return new MultiAssignment(children[0], elements);
    }
}
