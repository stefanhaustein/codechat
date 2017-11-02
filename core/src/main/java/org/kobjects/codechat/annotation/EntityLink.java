package org.kobjects.codechat.annotation;

import java.lang.ref.WeakReference;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.SerializationContext;

public class EntityLink implements Link {
    private final WeakReference<Entity> entity;

    public EntityLink(Entity entity) {
        this.entity = new WeakReference<Entity>(entity);
    }


    @Override
    public void execute(Environment environment) {
        StringBuilder sb = new StringBuilder();
        Entity entity = this.entity.get();
        if (entity == null) {
            environment.environmentListener.print("(deleted)");
        } else {
            entity.serialize(new AnnotatedStringBuilder(sb, null), new SerializationContext(environment, SerializationContext.Mode.EDIT));
            environment.environmentListener.edit(sb.toString());
        }
    }
}
