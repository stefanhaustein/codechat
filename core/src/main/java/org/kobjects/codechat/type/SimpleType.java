package org.kobjects.codechat.type;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.lang.Documented;

public class SimpleType extends Type implements Documented {
    private final String name;
    private final Class<?> javaClass;
    private final String documentation;

    SimpleType(String name, Class<?> javaClass, String documentation) {
        this.name = name;
        this.javaClass = javaClass;
        this.documentation = documentation;
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        return javaClass == Object.class || ((other instanceof SimpleType) && ((SimpleType) other).javaClass == javaClass);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AnnotatedCharSequence getDocumentation() {
        return new AnnotatedString(documentation, null);
    }
}
