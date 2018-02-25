package org.kobjects.codechat.annotation;

import java.lang.ref.WeakReference;
import org.kobjects.codechat.lang.Documentation;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.Classifier;

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
        if ((entity.getType() instanceof Classifier) && !((Classifier) entity.getType()).isInstantiable()) {
            return Documentation.getDocumentation(environment, (Classifier) entity.getType());
        }
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
        entity.print(asb, Printable.Flavor.EDIT);
        return asb.build();
    }

    @Override
    public void execute(Environment environment) {
        RootVariable entity = this.entity.get();
        if (entity == null) {
            environment.environmentListener.print("(deleted)", EnvironmentListener.Channel.OUTPUT);
        } else if ((entity.getType() instanceof Classifier) && !((Classifier) entity.getType()).isInstantiable()) {
            environment.environmentListener.print(Documentation.getDocumentation(environment, (Classifier) entity.getType()), EnvironmentListener.Channel.OUTPUT);
        } else {
            environment.environmentListener.print(getText(environment).toString(), EnvironmentListener.Channel.EDIT);
        }
    }
}
