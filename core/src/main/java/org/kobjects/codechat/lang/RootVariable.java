package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.parser.ParsingEnvironment;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;

public class RootVariable implements Entity, HasDependencies, Documented {
    private final ParsingEnvironment environment;
    public final String name;
    public Type type;
    public Object value;
    public boolean constant;
    public boolean builtin;
    public String unparsed;
    public String documentation;
    public Exception error;

    public RootVariable(ParsingEnvironment environment, String name, Type type, boolean constant) {
        this.environment = environment;
        this.name = name;
        this.type = type;
        this.constant = constant;
    }

    public void dump(StringBuilder sb) {
        sb.append(name);
        sb.append(" = ");
        sb.append(Formatting.toLiteral(value));
        sb.append(";\n");
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        if (serializationContext.isSerialized(this)) {
            return;
        }
        serializationContext.setSerialized(this);
        if (builtin || value == null) {
            return;
        }

        if (documentation != null) {
            for (String s: documentation.split("\n")) {
                asb.append("# ").append(s).append('\n');
            }
        }

        if (unparsed != null) {
            asb.append(unparsed);
            return;
        }

        if (constant && ((value instanceof UserFunction) || (value instanceof Instance && serializationContext.getMode() == SerializationContext.Mode.EDIT))) {
            Entity entity = (Entity) value;
            entity.serialize(asb, serializationContext);
        } else {
            if (serializationContext.getMode() != SerializationContext.Mode.EDIT) {
                asb.append(constant ? "let " : "variable ");
            }
            asb.append(name, new EntityLink(this));
            if (value instanceof Entity && serializationContext.getMode() == SerializationContext.Mode.LIST) {
                asb.append(": ");
                asb.append(Formatting.toLiteral(type));
                asb.append('\n');serializationContext.setSerialized((Entity) value);
            } else {
                asb.append(" = ");
                if (value instanceof Entity && !serializationContext.isSerialized((Entity) value)) {
                    Entity entity = (Entity) value;
                    entity.serialize(asb, serializationContext);
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

    @Override
    public void printDocumentation(AnnotatedStringBuilder asb) {
        if (value instanceof Function) {
            ((FunctionType) type).serializeSignature(asb, -1, name, null, null);
            asb.append("\n");
        } else if (type instanceof InstanceType && !((InstanceType) type).isInstantiable()) {
            ((InstanceType) type).printDocumentation(asb);
        } else if (!(type instanceof MetaType)) {
            asb.append(constant ? "constant ": "variable ");
            asb.append(name).append(": ");
            asb.append(type.toString(), type instanceof Documented ? new DocumentedLink((Documented) type) : null);
            if (constant) {
                asb.append(" = ");
                Formatting.toLiteral(asb, value);
            }
            asb.append("\n");
        }
        if (value instanceof Documented) {
            ((Documented) value).printDocumentation(asb);
        }
        if (documentation != null) {
            asb.append(documentation);
            asb.append("\n");
        }
    }

    public void delete() {
        environment.removeVariable(name);
        if (value instanceof Instance) {
            ((Instance) value).delete();
        }
        value = null;
    }

    @Override
    public Type getType() {
        return type;
    }
}
