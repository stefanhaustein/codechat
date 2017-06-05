package org.kobjects.codechat.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.kobjects.codechat.lang.ListType;
import org.kobjects.codechat.lang.CollectionType;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.SetType;
import org.kobjects.codechat.lang.Type;

public class CollectionLiteral extends Expression {
    Expression[] elements;
    CollectionType type;
    Class collectionTypeClass;

    public CollectionLiteral(Class collectionTypeClass, Expression... elements) {
        this.collectionTypeClass = collectionTypeClass;
        this.elements = elements;
    }

    @Override
    public Object eval(EvaluationContext context) {
        Collection<Object> result;
        if (type instanceof ListType) {
            result = new ArrayList<>(elements.length);
        } else if (Comparable.class.isAssignableFrom(type.elementType.getJavaClass())) {
            result = new TreeSet<>();
        } else {
            result = new LinkedHashSet<>();
        }
        for (int i = 0; i < elements.length; i++) {
            result.add(elements[i].eval(context));
        }
        return result;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        Type elementType;
        if (elements.length == 0) {
            elementType = Type.forJavaType(Object.class);
        } else {
            elements[0] = elements[0].resolve(parsingContext);
            elementType = elements[0].getType();
            for (int i = 1; i < elements.length; i++) {
                elements[i] = elements[i].resolve(parsingContext);
                if (!elementType.equals(elements[i].getType())) {
                    throw new RuntimeException(
                            "Type mismatch for list element " + i + " (" + elements[i] + "): " + elements[i].getType()
                                    + " expected: " + elementType);
                }
            }
        }
        type = collectionTypeClass == SetType.class ? new SetType(elementType) : new ListType(elementType);
        return this;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(collectionTypeClass == SetType.class ? "set[" : "list[");
        if (elements.length > 0) {
            elements[0].toString(sb, indent);
            for (int i = 1; i < elements.length; i++) {
                sb.append(", ");
                elements[i].toString(sb, indent);
            }
        }
        sb.append(']');
    }

    @Override
    public int getChildCount() {
        return elements.length;
    }

    @Override
    public Expression getChild(int index) {
        return elements[index];
    }
}
