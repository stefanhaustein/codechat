package org.kobjects.codechat.annotation;

import java.lang.ref.WeakReference;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.InstanceType;

public class VariableLink implements Link {
    public final WeakReference<RootVariable> entity;

    public VariableLink(RootVariable entity) {
        this.entity = new WeakReference<RootVariable>(entity);
    }

    public CharSequence getText(Environment environment) {
        RootVariable entity = this.entity.get();
        if (entity == null) {
            return "(deleted)";
        }
        if ((entity.getType() instanceof InstanceType) && !((InstanceType) entity.getType()).isInstantiable()) {
            return Formatting.getDocumentation((InstanceType) entity.getType());
        }
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
        entity.print(asb, Printable.Flavor.EDIT);
        return asb.build();
    }

    @Override
    public void execute(Environment environment) {
        RootVariable entity = this.entity.get();
        if (entity == null) {
            environment.environmentListener.print("(deleted)");
        } else if ((entity.getType() instanceof InstanceType) && !((InstanceType) entity.getType()).isInstantiable()) {
            environment.environmentListener.print(Formatting.getDocumentation((InstanceType) entity.getType()));
        } else {
            environment.environmentListener.edit(getText(environment).toString());
        }
    }
}
