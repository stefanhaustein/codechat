package org.kobjects.codechat.expr;


import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.CollectionType;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class CollectionLiteral extends Expression {
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
        Object[] data = new Object[elements.length];
        for (int i = 0; i < elements.length; i++) {
            data[i] = elements[i].eval(context);
        }
        return new Collection(type, data);
    }

    @Override
    public CollectionType getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
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

    @Override
    public CollectionLiteral reconstruct(Expression... children) {
        return new CollectionLiteral(collectionTypeClass, children);
    }
}
