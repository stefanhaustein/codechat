package org.kobjects.codechat.expr.unresolved;

import java.util.LinkedHashMap;
import java.util.Map;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.InstanceReference;
import org.kobjects.codechat.expr.ObjectLiteral;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.lang.Parser.PRECEDENCE_PATH;

public class UnresolvedObjectLiteral extends UnresolvedExpression {

    String typeName;
    TupleType type;
    int id;
    LinkedHashMap<String, UnresolvedExpression> elements;

    public UnresolvedObjectLiteral(UnresolvedExpression base, LinkedHashMap<String, UnresolvedExpression> elements) {
        this.elements = elements;
        if (base instanceof UnresolvedIdentifier) {
            typeName = ((UnresolvedIdentifier) base).name;
            id = -1;
        } else if (base instanceof UnresolvedInstanceReference) {
            typeName = ((UnresolvedInstanceReference) base).typeName;
            id = ((UnresolvedInstanceReference) base).id;
        } else {
            throw new RuntimeException("Object literal base must be a type or instance reference.");
        }
    }


    @Override
    public ObjectLiteral resolve(ParsingContext parsingContext) {
        type = (TupleType) parsingContext.environment.resolveType(typeName);

        LinkedHashMap<String, Expression> resolvedElements = new LinkedHashMap<>();
        for (String key: elements.keySet()) {
            Expression resolved = elements.get(key).resolve(parsingContext);

            TupleType.PropertyDescriptor property = type.getProperty(key);

            if (!property.writable) {
                throw new RuntimeException("Can't set read-only property " + key);
            }
            if (!property.type.isAssignableFrom(resolved.getType())) {
                throw new RuntimeException(key + " can't be assigned to type " + resolved.getType());
            }

            resolvedElements.put(key, resolved);
        }
        return new ObjectLiteral(type, id, resolvedElements);
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
        for (Map.Entry<String,UnresolvedExpression> entry : elements.entrySet()) {
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
}
