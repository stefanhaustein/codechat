package org.kobjects.codechat.annotation;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import org.kobjects.codechat.lang.Dependency;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;

public class InstanceLink implements Link {
    private final WeakReference<Instance> instance;

    public InstanceLink(Instance instance) {
        this.instance = new WeakReference<Instance>(instance);
    }


    @Override
    public void execute(Environment environment) {
        StringBuilder sb = new StringBuilder();
        Instance instance = this.instance.get();
        if (instance == null) {
            environment.environmentListener.print("(deleted)");
        } else {
            instance.serialize(new AnnotatedStringBuilder(sb, null), Instance.Detail.DETAIL, new HashMap<Dependency, Environment.SerializationState>());
            environment.environmentListener.edit(sb.toString());
        }
    }
}
