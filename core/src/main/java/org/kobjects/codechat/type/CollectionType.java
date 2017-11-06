package org.kobjects.codechat.type;

import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.lang.Documented;
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

    public String getName() {
        return name + "[" + elementType + "]";
    }

    @Override
    public AnnotatedCharSequence getDocumentation() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append("A ").append(name).append(" of ");
        if (elementType instanceof Documented) {
            asb.append(elementType.getName(), new DocumentedLink((Documented) elementType));
        } else {
            asb.append(elementType.getName());
        }
        asb.append(super.getDocumentation());
        return asb.build();
    }


    public Collection createInstance(Environment environment, int id) {
        return new Collection(environment, id, this);
    }
}
