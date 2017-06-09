package org.kobjects.codechat.expr;

import java.util.LinkedHashMap;
import java.util.Map;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.InstanceType;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;

import static org.kobjects.codechat.lang.Parser.PRECEDENCE_PATH;

public class ObjectLiteral extends Expression {

    String typeName;
    InstanceType type;
    int id;
    LinkedHashMap<String, Expression> elements;

    public ObjectLiteral(Expression base, LinkedHashMap<String, Expression> elements) {
        this.elements = elements;
        if (base instanceof Identifier) {
            typeName = ((Identifier) base).name;
            id = -1;
        } else if (base instanceof InstanceReference) {
            typeName = ((InstanceReference) base).typeName;
            id = ((InstanceReference) base).id;
        } else {
            throw new RuntimeException("Object literal base must be a type or instance reference.");
        }
    }

    @Override
    public Object eval(EvaluationContext context) {
        Instance instance = context.environment.instantiate(type.getJavaClass(), id);
        for (Map.Entry<String,Expression> entry : elements.entrySet()) {
            type.getProperty(entry.getKey()).set(instance, entry.getValue().eval(context));
        }
        return instance;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        type = (InstanceType) parsingContext.environment.resolveType(typeName);
        for (String key: elements.keySet()) {
            Expression resolved = elements.get(key).resolve(parsingContext);

            InstanceType.Property property = type.getProperty(key);

            if (!property.isMutable()) {
                throw new RuntimeException("Can't set read-only property " + key);
            }
            if (!property.type.isAssignableFrom(resolved.getType())) {
                throw new RuntimeException(key + " can't be assigned to type " + resolved.getType());
            }

            elements.put(key, resolved);
        }
        return this;
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
        sb.append(typeName);
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
