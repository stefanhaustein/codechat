package org.kobjects.codechat.type;

import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.lang.Environment;

public abstract class CollectionType extends InstanceType<Collection> {

    public final Type elementType;
    private final String name;

    public CollectionType(String name, Type elementType) {
        this.elementType = elementType;
        this.name = name;
        addProperty(0, "size", Type.NUMBER, false, "The number of contained elements.");
    }

    public boolean isAssignableFrom(Type other) {
        if (!(other instanceof CollectionType)) {
            return false;
        }
        CollectionType otherType = (CollectionType) other;
        return otherType.getClass() == getClass() && elementType.isAssignableFrom(otherType.elementType);
    }

    @Override
    public String getName() {
        return name + "[" + elementType.getName() + "]";
    }

    public void printDocumentationBody(AnnotatedStringBuilder asb, Environment environment) {
        asb.append("A ").append(name).append(" of ");
        asb.append(elementType.toString(), new DocumentedLink(elementType));
    }


    public Collection createInstance(Environment environment) {
        return new Collection(environment, this);
    }
}
