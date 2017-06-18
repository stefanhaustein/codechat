package org.kobjects.codechat.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.CollectionType;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.SimpleType;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.lang.Parser.PRECEDENCE_PATH;

public class CollectionLiteral extends AbstractResolved {
    Expression[] elements;
    CollectionType type;
    Class collectionTypeClass;

    public CollectionLiteral(Class collectionTypeClass, Expression... elements) {
        this.collectionTypeClass = collectionTypeClass;
        this.elements = elements;
        Type elementType;
        if (elements.length == 0) {
            elementType = Type.ANY;
        } else {
            elementType = elements[0].getType();
            for (int i = 1; i < elements.length; i++) {
                if (!elementType.equals(elements[i].getType())) {
                    throw new RuntimeException(
                            "Type mismatch for list element " + i + " (" + elements[i] + "): " + elements[i].getType()
                                    + " expected: " + elementType);
                }
            }
        }
        type = collectionTypeClass == SetType.class ? new SetType(elementType) : new ListType(elementType);
    }

    @Override
    public Object eval(EvaluationContext context) {
        Collection<Object> result;
        if (type instanceof ListType) {
            result = new ArrayList<>(elements.length);
        } else if (type.elementType instanceof SimpleType) {
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
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(collectionTypeClass == SetType.class ? "set(" : "list(");
        if (elements.length > 0) {
            elements[0].toString(sb, indent);
            for (int i = 1; i < elements.length; i++) {
                sb.append(", ");
                elements[i].toString(sb, indent);
            }
        }
        sb.append(')');
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
