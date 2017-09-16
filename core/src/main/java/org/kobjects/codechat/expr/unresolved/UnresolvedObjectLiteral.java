package org.kobjects.codechat.expr.unresolved;

import java.util.LinkedHashMap;
import java.util.Map;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.ObjectLiteral;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class UnresolvedObjectLiteral extends UnresolvedExpression {

    String typeName;
    TupleType type;
    int id;
    LinkedHashMap<String, UnresolvedExpression> elements;

    public UnresolvedObjectLiteral(int end, UnresolvedExpression base, LinkedHashMap<String, UnresolvedExpression> elements) {
        super(base.start, end);
        this.elements = elements;
        if (base instanceof UnresolvedIdentifier) {
            typeName = ((UnresolvedIdentifier) base).name;
            id = -1;
        } else if (base instanceof UnresolvedInstanceReference) {
            typeName = ((UnresolvedInstanceReference) base).typeName;
            id = ((UnresolvedInstanceReference) base).id;
        } else {
            throw new ExpressionParser.ParsingException(base.start, end, "Object literal base must be a type or instance reference.", null);
        }
    }


    @Override
    public ObjectLiteral resolve(ParsingContext parsingContext, Type expectedType) {
        type = (TupleType) parsingContext.environment.resolveType(typeName);

        LinkedHashMap<String, Expression> resolvedElements = new LinkedHashMap<>();
        for (String key: elements.keySet()) {
            UnresolvedExpression unresolved = elements.get(key);
            Expression resolved = unresolved.resolve(parsingContext, null);

            TupleType.PropertyDescriptor property = type.getProperty(key);

            if (!property.writable) {
                throw new ExpressionParser.ParsingException(unresolved.start - key.length(), unresolved.start, "Can't set read-only property " + key, null);
            }
            if (!property.type.isAssignableFrom(resolved.getType())) {
                throw new ExpressionParser.ParsingException(unresolved.start, unresolved.end, key + " can't be assigned to type " + resolved.getType(), null);
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
