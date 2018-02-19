package org.kobjects.codechat.annotation;

import java.lang.ref.WeakReference;
import org.kobjects.codechat.lang.Documentation;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.type.InstanceType;

public class InstanceLink implements Link {
    public final WeakReference<Instance> instance;

    public InstanceLink(Instance entity) {
        this.instance = new WeakReference<Instance>(entity);
    }

    public CharSequence getText(Environment environment) {
        Instance instance = this.instance.get();
        if (instance == null) {
            return "(deleted)";
        }
        if ((instance.getType() instanceof InstanceType) && !((InstanceType) instance.getType()).isInstantiable()) {
            return Documentation.getDocumentation(environment, instance.getType());
        }
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
        instance.print(asb, Printable.Flavor.EDIT);
        return asb.build();
    }

    @Override
    public void execute(Environment environment) {
        Instance instance = this.instance.get();
        if (instance == null) {
            environment.environmentListener.print("(deleted)", EnvironmentListener.Channel.OUTPUT);
        } else if ((instance.getType() instanceof InstanceType) && !((InstanceType) instance.getType()).isInstantiable()) {
            environment.environmentListener.print(Documentation.getDocumentation(environment, instance.getType()), EnvironmentListener.Channel.OUTPUT);
        } else {
            environment.environmentListener.print(getText(environment).toString(), EnvironmentListener.Channel.EDIT);
        }
    }
}
