package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.type.Type;

public class RootVariable implements Entity, HasDependencies {
    public String name;
    public Type type;
    public Object value;
    public boolean constant;
    public boolean builtin;
    public String unparsed;

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

        if (constant && ((value instanceof UserFunction) || (value instanceof Instance && serializationContext.getMode() == SerializationContext.Mode.EDIT))) {
            Entity entity = (Entity) value;
            entity.serialize(asb, serializationContext);
        } else {
            if (serializationContext.getMode() == SerializationContext.Mode.SAVE) {
                asb.append(constant ? "let " : "variable ");
            }
            asb.append(name, new EntityLink(this));
            asb.append(" = ");
            if (value instanceof Entity && !serializationContext.isSerialized((Entity) value)) {
               Entity entity = (Entity) value;
               entity.serialize(asb, serializationContext);
            } else {
               Formatting.toLiteral(asb, value);
               asb.append(";\n");
            }
        }
    }

    @Override
    public void setUnparsed(String unparsed) {
        this.unparsed = unparsed;
    }

    @Override
    public String getUnparsed() {
        return unparsed;
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        if (value instanceof HasDependencies) {
            ((HasDependencies) value).getDependencies(result);
        }
    }
}
