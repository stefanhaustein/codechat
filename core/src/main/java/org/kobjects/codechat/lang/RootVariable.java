package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.annotation.Title;
import org.kobjects.codechat.annotation.VariableLink;
import org.kobjects.codechat.parser.ParsingEnvironment;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.Typed;

public class RootVariable implements HasDependencies, Printable {
    private final ParsingEnvironment parsingEnvironment;
    public final String name;
    public Type type;
    public Object value;
    public boolean constant;
    public boolean builtin;
    public String unparsed;
    public CharSequence documentation;
    public Exception error;

    public RootVariable(ParsingEnvironment environment, String name, Type type, boolean constant) {
        this.parsingEnvironment = environment;
        this.name = name;
        this.type = type;
        this.constant = constant;
    }

    public void dump(StringBuilder sb) {
        sb.append(name);
        sb.append(" := ");
        sb.append(Formatting.toLiteral(value));
        sb.append(";\n");
    }


    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        if (builtin) {
            if (value instanceof Instance) {
                serializationContext.setSerialized((Instance) value);
            }
            return;
        }

        if (documentation != null) {
            for (String s: String.valueOf(documentation).split("\n")) {
                asb.append("# ").append(s).append('\n');
            }
        }

        if (unparsed != null) {
            asb.append(unparsed);
            return;
        }

        if (constant
                && value instanceof Instance
                && name.equals(parsingEnvironment.getConstantName((Instance) value))) {
            Instance instance = (Instance) value;
            serializationContext.setSerialized(instance);
            instance.print(asb, serializationContext.getMode());
        } else {
            if (serializationContext.getMode() != Printable.Flavor.EDIT) {
                asb.append(constant ? "let " : "variable ");
            }
            asb.append(name, new VariableLink(this));
            if (value instanceof Instance && serializationContext.getMode() == Printable.Flavor.LIST) {
                asb.append(": ");
                asb.append(Formatting.toLiteral(type));
                asb.append('\n');
                serializationContext.setSerialized((Instance) value);
            } else {
                asb.append(" := ");
                if (value instanceof Instance && !serializationContext.isSerialized((Instance) value)) {
                    Instance instance = (Instance) value;
                    serializationContext.setSerialized(instance);
                    instance.print(asb, serializationContext.getMode());
                } else {
                    Formatting.toLiteral(asb, value);
                    asb.append('\n');
                }
            }
        }
    }

    public void setUnparsed(String unparsed) {
        this.unparsed = unparsed;
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        if (value instanceof HasDependencies) {
            ((HasDependencies) value).getDependencies(result);
        }
    }

    public void delete() {
        parsingEnvironment.removeVariable(name);
        if (value instanceof Instance) {
            ((Instance) value).delete();
        }
        value = null;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void print(AnnotatedStringBuilder asb, Flavor flavor) {
        serialize(asb, new SerializationContext(flavor));
    }
}
