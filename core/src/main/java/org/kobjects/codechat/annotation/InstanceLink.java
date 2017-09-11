package org.kobjects.codechat.annotation;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.SerializationContext;

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
            instance.serialize(new AnnotatedStringBuilder(sb, null), SerializationContext.Detail.DETAIL, new SerializationContext());
            environment.environmentListener.edit(sb.toString());
        }
    }
}
