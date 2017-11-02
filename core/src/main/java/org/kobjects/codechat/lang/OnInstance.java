package org.kobjects.codechat.lang;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.PropertyAccess;

import java.util.ArrayList;
import java.util.List;

import org.kobjects.codechat.type.Type;

public class OnInstance implements Instance, Property.PropertyListener {
    private List<Property> properties = new ArrayList<>();
    private Object lastValue = Boolean.FALSE;
    private OnExpression onExpression;
    private EvaluationContext contextTemplate;
    private int id;
    private Timer timer;
    private String unparsed;
    private OnExpression.Kind kind;
    static Set<OnInstance> allOnInterval = new HashSet<OnInstance>();

    public OnInstance(Environment environment, int id, OnExpression.Kind kind) {
        this.id = id;
        this.kind = kind;
    }

    public void init(OnExpression onExpression, final EvaluationContext contextTemplate) {
        detach();
        this.onExpression = onExpression;
        this.contextTemplate = contextTemplate;
        if (onExpression.kind == OnExpression.Kind.ON_INTERVAL) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!contextTemplate.environment.paused) {
                        EvaluationContext evalContext = OnInstance.this.contextTemplate.clone();
                        OnInstance.this.onExpression.body.eval(evalContext);
                    }
                }
            }, 0, Math.round(((Number) onExpression.expression.eval(contextTemplate)).doubleValue()*1000));
            synchronized (allOnInterval) {
                allOnInterval.add(this);
            }
        } else {
            addAll(onExpression.expression, contextTemplate);
        }
    }

    @Override
    public void valueChanged(Property property, Object oldValue, Object newValue) {
        switch (onExpression.kind) {
            case ON_CHANGE: {
                EvaluationContext evalContext = contextTemplate.clone();
                onExpression.body.eval(evalContext);
                break;
            }
            case ON: {
                Object conditionValue = onExpression.expression.eval(contextTemplate);
                if (!conditionValue.equals(lastValue)) {
                    lastValue = conditionValue;
                    if (Boolean.TRUE.equals(conditionValue)) {
                        EvaluationContext evalContext = contextTemplate.clone();
                        onExpression.body.eval(evalContext);
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException();
        }
    }

    public void detach() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            synchronized (allOnInterval) {
                allOnInterval.remove(this);
            }
        }
        for (Property property : properties) {
            property.removeListener(this);
        }
    }

    private void addAll(Expression expr, EvaluationContext context) {
        if (expr instanceof PropertyAccess) {
            Property property = ((PropertyAccess) expr).getProperty(context);
            property.addListener(this);
            properties.add(property);
        }
        for (int i = 0; i < expr.getChildCount(); i++) {
            addAll(expr.getChild(i), context);
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void setUnparsed(String unparsed) {
        this.unparsed = unparsed;
    }

    @Override
    public String getUnparsed() {
        return unparsed;
    }


    public void delete() {
        detach();
    }


    @Override
    public OnInstanceType getType() {
        return kind.type;
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializationContext.setSerialized(this);

        boolean wrap = onExpression.closure.toString(asb.getStringBuilder(), contextTemplate);

        asb.append(onExpression.getType().getName().toLowerCase() + "#" + String.valueOf(getId()), new EntityLink(this));
        asb.append(" ").append(onExpression.expression.toString()).append(":\n");
        onExpression.body.toString(asb, wrap ? 2 : 1);
        if (wrap) {
            asb.append("  end;\n");
        }
        asb.append("end;\n");
    }

    public static class OnInstanceType extends Type {
        private final String name;
        public OnExpression.Kind kind;

        public OnInstanceType(String name) {
            this.name = name;
        }

        @Override
        public OnInstance createInstance(Environment environment, int id) {
            return new OnInstance(environment, id, kind);
        }

        @Override
        public boolean isAssignableFrom(Type other) {
            return false;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        if (onExpression != null) {
            onExpression.getDependencies(result);
        }
    }
}
