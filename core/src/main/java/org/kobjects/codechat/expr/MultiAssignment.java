package org.kobjects.codechat.expr;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.instance.Instance;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class MultiAssignment extends Expression {

    final Expression base;
    final LinkedHashMap<Classifier.PropertyDescriptor, Expression> elements;

    public MultiAssignment(Expression base, LinkedHashMap<Classifier.PropertyDescriptor, Expression> elements) {
        this.base = base;
        this.elements = elements;
    }

    @Override
    public Object eval(EvaluationContext context) {
        Instance instance = (Instance) base.eval(context);
        Classifier type = (Classifier) base.getType();
        for (Map.Entry<Classifier.PropertyDescriptor,Expression> entry : elements.entrySet()) {
            entry.getKey().set(instance, entry.getValue().eval(context));
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
        for (Map.Entry<Classifier.PropertyDescriptor,Expression> entry : elements.entrySet()) {
            sb.append(materializedIndent);
            sb.append(entry.getKey().name).append(" := ");
            entry.getValue().toString(sb, indent + 2);
            sb.append("\n");
        }
        sb.append(Formatting.space(indent));
        sb.append("end ");    }

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
