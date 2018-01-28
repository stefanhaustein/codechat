package org.kobjects.codechat.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.Title;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.UserMethod;
import org.kobjects.codechat.statement.HelpStatement;

public abstract class InstanceType<T extends Instance> extends AbstractType implements Documented {
    private final TreeMap<String, PropertyDescriptor> propertyMap = new TreeMap<>();
    final ArrayList<UserMethod> methods = new ArrayList<>();

    private final boolean singleton;

    public InstanceType() {
        this(false);
    }

    public InstanceType(boolean singleton) {
        this.singleton = singleton;
    }

    public InstanceType addProperty(int index, String name, Type type, boolean writable, CharSequence documentation, Expression initializer) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation, initializer));
        return this;
    }

    public InstanceType addProperty(int index, String name, Type type, boolean writable, CharSequence documentation) {
        propertyMap.put(name, new PropertyDescriptor(name, type, index, writable, documentation, null));
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
        return other instanceof InstanceType && other.toString().equals(toString());
    }

    @Override
    public final void printDocumentation(AnnotatedStringBuilder asb) {
        printDocumentationBody(asb);
        asb.append("\n\nProperties:\n");
        boolean first = true;
        for (PropertyDescriptor propertyDescriptor: properties()) {
            asb.append("\n- ");
            asb.append(propertyDescriptor.name, new DocumentedLink(propertyDescriptor));
        }
    }

    public void printDocumentationBody(AnnotatedStringBuilder asb) {
    }

    public boolean hasProperty(String propertyName) {
        return propertyMap.containsKey(propertyName);
    }

    public void printBody(AnnotatedStringBuilder asb) {
        for (PropertyDescriptor descriptor : properties()) {
            descriptor.print(asb, Printable.Flavor.DEFAULT);
        }
        for (UserMethod method : methods) {
            method.toString(asb, 2);
        }
    }

    public void addMethod(UserMethod userMethod) {
        methods.add(userMethod);
    }

    public boolean hasMethod(String propertyName) {
        for (UserMethod method: methods) {
            if (method.name.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public UserMethod getMethod(String name) {
        for (UserMethod method: methods) {
            if (method.name.equals(name)) {
                return method;
            }
        }
        throw new  IllegalArgumentException("Method '" + name + "' does not exist");
    }

    public class PropertyDescriptor implements Documented, Printable {
        public final String name;
        public final Type type;
        public final int index;
        public final boolean writable;
        public final CharSequence documentation;
        public final Expression initializer;

        private PropertyDescriptor(String name, Type type, int index, boolean writable, CharSequence documentation, Expression initializer) {
            this.name = name;
            this.type = type;
            this.index = index;
            this.writable = writable;
            this.documentation = HelpStatement.examplify(documentation);
            this.initializer = initializer;
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
        public void printDocumentation(AnnotatedStringBuilder asb) {
            String ownerName = singleton ? InstanceType.this.toString().toLowerCase() : InstanceType.this.toString();

            asb.append(ownerName + "." + name + "\n\n", new Title());
            asb.append("Owner: ");
            asb.append(ownerName, new DocumentedLink(InstanceType.this));
            asb.append("\nType: ");
            asb.append(type.toString(), type instanceof Documented ? new DocumentedLink((Documented) type) : null);
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
