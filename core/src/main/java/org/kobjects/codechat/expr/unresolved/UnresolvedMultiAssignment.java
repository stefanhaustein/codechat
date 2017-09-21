package org.kobjects.codechat.expr.unresolved;

import java.util.LinkedHashMap;
import java.util.Map;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.MultiAssignment;
import org.kobjects.codechat.expr.ObjectLiteral;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class UnresolvedMultiAssignment extends UnresolvedExpression {

    UnresolvedExpression base;
    LinkedHashMap<String, UnresolvedExpression> elements;

    public UnresolvedMultiAssignment(UnresolvedExpression base, LinkedHashMap<String, UnresolvedExpression> elements, int end) {
        super(base.start, end);
        this.base = base;
        this.elements = elements;
    }


    @Override
    public MultiAssignment resolve(ParsingContext parsingContext, Type expectedType) {
        Expression resolvedBase = base.resolve(parsingContext, expectedType);

        if (resolvedBase.getType() instanceof TupleType) {
            throw new ExpressionParser.ParsingException(base.start, base.end, "Multi-assignment base expression must be of tuply type.", null);
        }
        TupleType type = (TupleType) resolvedBase.getType();

        LinkedHashMap<String, Expression> resolvedElements = new LinkedHashMap<>();
        for (String key: elements.keySet()) {
            TupleType.PropertyDescriptor property = type.getProperty(key);

            UnresolvedExpression unresolved = elements.get(key);
            if (!property.writable) {
                throw new ExpressionParser.ParsingException(unresolved.start - key.length(), unresolved.start, "Can't set read-only property " + key, null);
            }
            Expression resolved = unresolved.resolve(parsingContext, property.type);
            if (!property.type.isAssignableFrom(resolved.getType())) {
                throw new ExpressionParser.ParsingException(unresolved.start, unresolved.end, key + " can't be assigned to type " + resolved.getType(), null);
            }
            resolvedElements.put(key, resolved);
        }
        return new MultiAssignment(resolvedBase, resolvedElements);
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
        for (Map.Entry<String,UnresolvedExpression> entry : elements.entrySet()) {
            sb.append(materializedIndent);
            sb.append(entry.getKey()).append(" = ");
            entry.getValue().toString(sb, indent + 2);
            sb.append(";\n");
        }
        sb.append(Formatting.space(indent));
        sb.append("end;\n");
    }
}
