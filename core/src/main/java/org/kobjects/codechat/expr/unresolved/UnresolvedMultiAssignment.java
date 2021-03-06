package org.kobjects.codechat.expr.unresolved;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.MultiAssignment;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class UnresolvedMultiAssignment extends UnresolvedExpression {

    public final UnresolvedExpression base;
    public final LinkedHashMap<String, UnresolvedExpression> elements;

    public UnresolvedMultiAssignment(UnresolvedExpression base, LinkedHashMap<String, UnresolvedExpression> elements, int end) {
        super(base.start, end);
        this.base = base;
        this.elements = elements;
    }


    @Override
    public MultiAssignment resolve(ParsingContext parsingContext, Type expectedType) {

        Set<Classifier.PropertyDescriptor> requiredFileds = new HashSet<>();

        boolean ctorBase = (base instanceof UnresolvedConstructor);
        Expression resolvedBase = ctorBase
            ? ((UnresolvedConstructor) base).resolve(parsingContext, true)
            : base.resolve(parsingContext, expectedType);

        if (!(resolvedBase.getType() instanceof Classifier)) {
            throw new ExpressionParser.ParsingException(base.start, base.end, "Multi-assignment base expression must be of tupe type (is: " + resolvedBase.getType() + ")", null);
        }
        Classifier type = (Classifier) resolvedBase.getType();


        LinkedHashMap<Classifier.PropertyDescriptor, Expression> resolvedElements = new LinkedHashMap<>();
        for (String key: elements.keySet()) {
            Classifier.PropertyDescriptor property = type.getProperty(key);

            UnresolvedExpression unresolved = elements.get(key);
            if (!property.writable) {
                throw new ExpressionParser.ParsingException(unresolved.start - key.length(), unresolved.start, "Can't set read-only property " + key, null);
            }
            Expression resolved = unresolved.resolve(parsingContext, property.type);
            if (!property.type.isAssignableFrom(resolved.getType())) {
                throw new ExpressionParser.ParsingException(unresolved.start, unresolved.end, key + " can't be assigned to type " + resolved.getType(), null);
            }
            resolvedElements.put(property, resolved);
        }

        if (ctorBase) {
            Iterable<Classifier.PropertyDescriptor> propertyDescriptors = type.properties();
            for (Classifier.PropertyDescriptor properyDescriptor : propertyDescriptors) {
                if (properyDescriptor.needsExplicitValue && !resolvedElements.containsKey(properyDescriptor)) {
                    throw new ExpressionParser.ParsingException(start, end, "Property '" + properyDescriptor.name + "' requires a value.", null);
                }
            }
        }

        return new MultiAssignment(resolvedBase, resolvedElements);
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
        for (Map.Entry<String,UnresolvedExpression> entry : elements.entrySet()) {
            sb.append(materializedIndent);
            sb.append(entry.getKey()).append(" := ");
            entry.getValue().toString(sb, indent + 2);
            sb.append('\n');
        }
        sb.append(Formatting.space(indent));
        sb.append("end\n");
    }
}
