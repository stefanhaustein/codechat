package org.kobjects.codechat.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.Title;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.HasDocumentationDetail;
import org.kobjects.codechat.instance.Instance;
import org.kobjects.codechat.instance.Method;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.instance.Property;
import org.kobjects.codechat.statement.HelpStatement;

public abstract class Classifier<T extends Instance> extends AbstractType implements HasDocumentationDetail {
    private final TreeMap<String, PropertyDescriptor> propertyMap = new TreeMap<>();
    final ArrayList<Method> methods = new ArrayList<>();

    private final boolean singleton;

    public Classifier() {
        this(false);
    }

    public Classifier(boolean singleton) {
        this.singleton = singleton;
    }

    public Classifier addProperty(int index, String name, Type type, boolean writable, CharSequence documentation, Expression initializer) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation, initializer, initializer == null));
        return this;
    }

    public Classifier addProperty(int index, String name, Type type, boolean writable, CharSequence documentation) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation, null, false));
        return this;
    }

    public PropertyDescriptor getProperty(String name) {
        PropertyDescriptor propertyDescriptor = propertyMap.get(name);
        if (propertyDescriptor == null) {
            throw new IllegalArgumentException("Property '" + name + "' does not exist");
        }
        return propertyDescriptor;
    }

    public Collection<PropertyDescriptor> properties() {
        return propertyMap.values();
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        return other instanceof Classifier && other.toString().equals(toString());
    }

    @Override
    public void printDocumentationDetail(AnnotatedStringBuilder asb) {
        asb.append("\n\nProperties:\n");
        boolean first = true;
        for (PropertyDescriptor propertyDescriptor: properties()) {
            asb.append("\n- ");
            asb.append(propertyDescriptor.name, new DocumentedLink(propertyDescriptor));
        }
    }



    public boolean hasProperty(String propertyName) {
        return propertyMap.containsKey(propertyName);
    }

    public void printBody(AnnotatedStringBuilder asb) {
        for (PropertyDescriptor descriptor : properties()) {
            descriptor.print(asb, Printable.Flavor.DEFAULT);
        }
        for (Method method : methods) {
            method.toString(asb, 2);
        }
    }

    public void addMethod(Method method) {
        methods.add(method);
    }

    public boolean hasMethod(String propertyName) {
        for (Method method: methods) {
            if (method.name.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public Method getMethod(String name) {
        for (Method method: methods) {
            if (method.name.equals(name)) {
                return method;
            }
        }
        throw new  IllegalArgumentException("Method '" + name + "' does not exist");
    }

    public class PropertyDescriptor implements Printable, HasDocumentationDetail {
        public final String name;
        public final Type type;
        public final int index;
        public final boolean writable;
        public final CharSequence documentation;
        public final Expression initializer;
        public final boolean needsExplicitValue;

        private PropertyDescriptor(String name, Type type, int index, boolean writable, CharSequence documentation, Expression initializer, boolean needsExplicitValue) {
            this.name = name;
            this.type = type;
            this.index = index;
            this.writable = writable;
            this.documentation = HelpStatement.examplify(documentation);
            this.initializer = initializer;
            this.needsExplicitValue = needsExplicitValue;
        }

        public Property getProperty(Instance tuple) {
            return tuple.getProperty(index);
        }

        public void set(Instance tuple, Object value) {
            tuple.getProperty(index).set(value);
        }

        public Object get(Instance tuple) {
            return tuple.getProperty(index).get();
        }

        @Override
        public void printDocumentationDetail(AnnotatedStringBuilder asb) {
            String ownerName = singleton ? Classifier.this.toString().toLowerCase() : Classifier.this.toString();

            asb.append(ownerName + "." + name + "\n\n", new Title());
            asb.append("Owner: ");
            asb.append(ownerName, new DocumentedLink(Classifier.this));
            asb.append("\nType: ");
            asb.append(type.getName(), new DocumentedLink(type));
            asb.append("\n\n");
            asb.append(documentation);
        }

        @Override
        public void print(AnnotatedStringBuilder asb, Flavor flavor) {
            asb.append("  ");
            asb.append(name);
            asb.append(" = ");
            initializer.toString(asb, 6);
            asb.append('\n');
        }
    }


    public boolean isInstantiable() {
        return !singleton;
    }

    public T createInstance(Environment environment) {
        throw new UnsupportedOperationException();
    }
}
